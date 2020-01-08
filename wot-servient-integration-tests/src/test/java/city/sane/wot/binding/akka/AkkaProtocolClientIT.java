package city.sane.wot.binding.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import city.sane.relay.server.RelayServer;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientNotImplementedException;
import city.sane.wot.binding.akka.Messages.*;
import city.sane.wot.binding.akka.actor.DiscoveryDispatcherActor;
import city.sane.wot.binding.akka.actor.ThingsActor;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.IntegerSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AkkaProtocolClientIT {
    private Thread relayServerThread;
    private RelayServer relayServer;

    private ActorSystem system;
    private AkkaProtocolClientFactory clientFactory;
    private ProtocolClient client;

    @Before
    public void setUp() {
        relayServer = new RelayServer(ConfigFactory.load());
        relayServerThread = new Thread(relayServer);
        relayServerThread.start();

        clientFactory = new AkkaProtocolClientFactory(ConfigFactory.load());
        clientFactory.init().join();

        client = clientFactory.getClient();

        system = ActorSystem.create("my-relayServer", ConfigFactory.load().getConfig("wot.servient.akka.server"));
    }

    @After
    public void tearDown() throws InterruptedException {
        clientFactory.destroy().join();

        if (system != null) {
            TestKit.shutdownActorSystem(system);
            system = null;
        }

        relayServer.close();
        relayServerThread.join();
    }

    @Test
    public void readResource() throws ExecutionException, InterruptedException, ContentCodecException {
        ActorRef actorRef = system.actorOf(Props.create(MyReadActor.class, MyReadActor::new));
        String href = actorRef.path().toStringWithAddress(system.provider().getDefaultAddress());
        Form form = new Form.Builder().setHref(href).build();

        Assert.assertEquals(ContentManager.valueToContent(1337), client.readResource(form).get());
    }

    @Test
    public void writeResource() throws ContentCodecException, ExecutionException, InterruptedException {
        ActorRef actorRef = system.actorOf(Props.create(MyWriteActor.class, MyWriteActor::new));
        String href = actorRef.path().toStringWithAddress(system.provider().getDefaultAddress());
        Form form = new Form.Builder().setHref(href).build();

        Assert.assertEquals(ContentManager.valueToContent(42), client.writeResource(form, ContentManager.valueToContent(1337)).get());
    }

    @Test
    public void invokeResource() throws ContentCodecException, ExecutionException, InterruptedException {
        ActorRef actorRef = system.actorOf(Props.create(MyInvokeActor.class, MyInvokeActor::new));
        String href = actorRef.path().toStringWithAddress(system.provider().getDefaultAddress());
        Form form = new Form.Builder().setHref(href).build();

        Assert.assertEquals(ContentManager.valueToContent(45), client.invokeResource(form, ContentManager.valueToContent(3)).get());
    }

    @Test(timeout = 20 * 1000L)
    public void subscribeResource() throws ExecutionException, InterruptedException, ProtocolClientNotImplementedException, ContentCodecException {
        ActorRef actorRef = system.actorOf(Props.create(MySubscribeActor.class, MySubscribeActor::new));
        String href = actorRef.path().toStringWithAddress(system.provider().getDefaultAddress());
        Form form = new Form.Builder().setHref(href).build();

        CompletableFuture<Content> future = new CompletableFuture<>();

        Observer<Content> observer = new Observer<>(next -> future.complete(next));
        client.subscribeResource(form, observer).get();

        assertEquals(ContentManager.valueToContent(9001), future.get());
    }

    @Test
    public void discover() throws ExecutionException, InterruptedException {
        ExposedThing exposedCounterThing = getExposedCounterThing();
        Map<String, ExposedThing> things = Map.of("counter", exposedCounterThing);
        system.actorOf(DiscoveryDispatcherActor.props());
        system.actorOf(ThingsActor.props(things), "things");

        Collection<Thing> discoveredThings = client.discover(new ThingFilter()).get();
        assertThat(discoveredThings, hasItem(exposedCounterThing));
    }

    private class MyReadActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder().match(Messages.Read.class, m -> {
                Content content = ContentManager.valueToContent(1337);
                getSender().tell(new RespondRead(content), getSelf());
            }).build();
        }
    }

    private class MyWriteActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder().match(Messages.Write.class, m -> {
                Content content = ContentManager.valueToContent(42);
                getSender().tell(new Written(content), getSelf());
            }).build();
        }
    }

    private class MyInvokeActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder().match(Invoke.class, m -> {
                Content content = ContentManager.valueToContent(45);
                getSender().tell(new Invoked(content), getSelf());
            }).build();
        }
    }

    private class MySubscribeActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder().match(Subscribe.class, m -> {
                Content content = ContentManager.valueToContent(9001);
                getSender().tell(new SubscriptionNext(content), getSelf());
            }).build();
        }
    }

    private class MyDiscoverActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder().match(DiscoveryDispatcherActor.Discover.class, m -> getSender().tell(new ThingsActor.Things(Collections.emptyMap()), getSelf())).build();
        }
    }

    private ExposedThing getExposedCounterThing() {
        ThingProperty counterProperty = new ThingProperty.Builder()
                .setType("integer")
                .setDescription("current counter content")
                .setObservable(true)
                .build();

        ThingProperty lastChangeProperty = new ThingProperty.Builder()
                .setType("string")
                .setDescription("last change of counter content")
                .setObservable(true)
                .setReadOnly(true)
                .build();

        ExposedThing thing = new ExposedThing(null)
                .setId("counter")
                .setTitle("counter");

        thing.addProperty("count", counterProperty, 42);
        thing.addProperty("lastChange", lastChangeProperty, new Date().toString());

        thing.addAction("increment",
                new ThingAction.Builder()
                        .setDescription("Incrementing counter content with optional step content as uriVariable")
                        .setUriVariables(Map.of(
                                "step", Map.of(
                                        "type", "integer",
                                        "minimum", 1,
                                        "maximum", 250
                                )
                        ))
                        .setInput(new ObjectSchema())
                        .setOutput(new IntegerSchema())
                        .build(),
                (input, options) -> {
                    return thing.getProperty("count").read().thenApply(value -> {
                        int step;
                        if (input != null && ((Map) input).containsKey("step")) {
                            step = (Integer) ((Map) input).get("step");
                        }
                        else if (options.containsKey("uriVariables") && ((Map) options.get("uriVariables")).containsKey("step")) {
                            step = (int) ((Map) options.get("uriVariables")).get("step");
                        }
                        else {
                            step = 1;
                        }
                        int newValue = ((Integer) value) + step;
                        thing.getProperty("count").write(newValue);
                        thing.getProperty("lastChange").write(new Date().toString());
                        thing.getEvent("change").emit();
                        return newValue;
                    });
                });

        thing.addAction("decrement", new ThingAction(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int newValue = ((Integer) value) - 1;
                thing.getProperty("count").write(newValue);
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return newValue;
            });
        });

        thing.addAction("reset", new ThingAction(), (input, options) -> {
            return thing.getProperty("count").write(0).thenApply(value -> {
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return 0;
            });
        });

        thing.addEvent("change", new ThingEvent());

        return thing;
    }
}