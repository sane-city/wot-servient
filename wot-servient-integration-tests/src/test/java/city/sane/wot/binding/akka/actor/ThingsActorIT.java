package city.sane.wot.binding.akka.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import city.sane.wot.binding.akka.Messages;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.IntegerSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

public class ThingsActorIT {
    private ActorSystem system;

    @Before
    public void setUp() {
        Config config = ConfigFactory.load().getConfig("wot.servient.akka");
        system = ActorSystem.create("my-server", config);
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void getThings() throws ContentCodecException {
        TestKit testKit = new TestKit(system);
        ActorRef actorRef = system.actorOf(ThingsActor.props(Map.of("counter", getExposedCounterThing())));

        actorRef.tell(new Messages.Read(), testKit.getRef());

        Messages.RespondRead msg = testKit.expectMsgClass(Messages.RespondRead.class);
        Map things = ContentManager.contentToValue(msg.content, new ObjectSchema());

        assertThat((Map<String, Thing>) things, hasKey("counter"));
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
                        else if (options.containsKey("uriVariables") && options.get("uriVariables").containsKey("step")) {
                            step = (int) options.get("uriVariables").get("step");
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

        thing.addAction("decrement", new ThingAction<Object, Object>(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int newValue = ((Integer) value) - 1;
                thing.getProperty("count").write(newValue);
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return newValue;
            });
        });

        thing.addAction("reset", new ThingAction<Object, Object>(), (input, options) -> {
            return thing.getProperty("count").write(0).thenApply(value -> {
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return 0;
            });
        });

        thing.addEvent("change", new ThingEvent<Object>());

        return thing;
    }

    @Test
    public void discover() {
        TestKit testKit = new TestKit(system);
        ActorRef actorRef = system.actorOf(ThingsActor.props(Map.of("counter", getExposedCounterThing())));

        ThingFilter filter = new ThingFilter();
        actorRef.tell(new ThingsActor.Discover(filter), testKit.getRef());

        ThingsActor.Things msg = testKit.expectMsgClass(ThingsActor.Things.class);

        assertThat(msg.entities, hasKey("counter"));
    }

//    @Test
//    public void created() {
//        TestKit testKit = new TestKit(system);
//        ActorRef actorRef = system.actorOf(ThingsActor.props(Map.of("counter", getExposedCounterThing())));
//
//        actorRef.tell(new ThingsActor.Created<>(new Pair(testKit.getRef(), "counter")), ActorRef.noSender());
//
//        testKit.expectMsgClass(ThingsActor.Created.class);
//    }

    @Test
    public void expose() {
        TestKit testKit = new TestKit(system);
        ActorRef actorRef = system.actorOf(ThingsActor.props(Map.of("counter", getExposedCounterThing())));

        actorRef.tell(new ThingsActor.Expose("counter"), testKit.getRef());

        testKit.expectMsgClass(ThingsActor.Created.class);
    }

//    @Test
//    public void deleted() {
//        TestKit testKit = new TestKit(system);
//        ActorRef actorRef = system.actorOf(ThingsActor.props(Map.of("counter", getExposedCounterThing())));
//
//        actorRef.tell(new ThingsActor.Deleted<>(new Pair(testKit.getRef(), "counter")), ActorRef.noSender());
//
//        testKit.expectMsgClass(ThingsActor.Deleted.class);
//    }

    @Test
    public void destroy() {
        TestKit testKit = new TestKit(system);
        ActorRef actorRef = system.actorOf(ThingsActor.props(Map.of("counter", getExposedCounterThing())));
        actorRef.tell(new ThingsActor.Expose("counter"), testKit.getRef());
        testKit.expectMsgClass(ThingsActor.Created.class);

        actorRef.tell(new ThingsActor.Destroy("counter"), testKit.getRef());

        testKit.expectMsgClass(ThingsActor.Deleted.class);
    }
}
