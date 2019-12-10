package city.sane.wot.binding.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.akka.actor.DiscoveryDispatcherActor;
import city.sane.wot.binding.akka.actor.ThingsActor;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.form.Form;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AkkaProtocolClientTest {
    private ActorSystem system;
    private AkkaProtocolClientFactory clientFactory;
    private ProtocolClient client;

    @Before
    public void setUp() {
        clientFactory = new AkkaProtocolClientFactory(ConfigFactory.load());
        clientFactory.init().join();

        client = clientFactory.getClient();

        Config config = ConfigFactory.parseString("akka.actor.provider = cluster").withFallback(ConfigFactory.load());
        system = ActorSystem.create("my-server", config);
    }

    @After
    public void tearDown() {
        clientFactory.destroy().join();

        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void readResource() throws ExecutionException, InterruptedException, ContentCodecException {
        ActorRef actorRef = system.actorOf(Props.create(MyReadActor.class, MyReadActor::new));
        String href = actorRef.path().toStringWithAddress(system.provider().getDefaultAddress());
        Form form = new Form.Builder().setHref(href).build();

        assertEquals(ContentManager.valueToContent(1337), client.readResource(form).get());
    }

    @Test
    public void writeResource() throws ContentCodecException, ExecutionException, InterruptedException {
        ActorRef actorRef = system.actorOf(Props.create(MyWriteActor.class, MyWriteActor::new));
        String href = actorRef.path().toStringWithAddress(system.provider().getDefaultAddress());
        Form form = new Form.Builder().setHref(href).build();

        assertEquals(ContentManager.valueToContent(42), client.writeResource(form, ContentManager.valueToContent(1337)).get());
    }

    @Test
    public void discover() throws ExecutionException, InterruptedException {
        system.actorOf(Props.create(MyDiscoverActor.class, MyDiscoverActor::new));

        assertThat(client.discover(new ThingFilter()).get(), instanceOf(Collection.class));
    }

    class MyReadActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder().match(Messages.Read.class, m -> {
                Content content = ContentManager.valueToContent(1337);
                getSender().tell(new Messages.RespondRead(content), getSelf());
            }).build();
        }
    }

    class MyWriteActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder().match(Messages.Write.class, m -> {
                Content content = ContentManager.valueToContent(42);
                getSender().tell(new Messages.Written(content), getSelf());
            }).build();
        }
    }

    class MyDiscoverActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder().match(DiscoveryDispatcherActor.Discover.class, m -> {
                getSender().tell(new ThingsActor.Things(Collections.emptyMap()), getSelf());
            }).build();
        }
    }
}