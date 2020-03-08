package city.sane.wot.binding.akka.actor;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import city.sane.wot.binding.akka.Message;
import city.sane.wot.binding.akka.actor.ThingActor.*;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.property.ExposedThingProperty;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ThingActorTest {
    private ActorSystem system;
    private ExposedThing thing;
    private Duration timeout;
    private ExposedThingProperty<Object> property;
    private ExposedThingEvent<Object> event;
    private ExposedThingAction<Object, Object> action;

    @Before
    public void setUp() {
        system = ActorSystem.create();
        thing = mock(ExposedThing.class);
        timeout = Duration.ofMillis(3000);
        property = mock(ExposedThingProperty.class);
        event = mock(ExposedThingEvent.class);
        action = mock(ExposedThingAction.class);
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void actorCreationShouldAddFormsToThing() {
        when(thing.getId()).thenReturn("counter");
        when(thing.getProperties()).thenReturn(Map.of("count", property));
        when(property.isObservable()).thenReturn(true);
        when(property.observer()).thenReturn(PublishSubject.create());
        when(thing.getActions()).thenReturn(Map.of("reset", action));
        when(thing.getEvents()).thenReturn(Map.of("changed", event));
        when(event.observer()).thenReturn(PublishSubject.create());

        final Props props = ThingActor.props(thing);
        TestActorRef.create(system, props);

        verify(thing, timeout(1 * 1000L)).addForm(any());
        verify(property, timeout(1 * 1000L)).addForm(any());
        verify(action, timeout(1 * 1000L)).addForm(any());
        verify(event, timeout(1 * 1000L)).addForm(any());
    }

    @Test
    public void getThingDescriptionShouldBeAnsweredWithThingDescription() throws ExecutionException, InterruptedException, ContentCodecException {
        ExposedThing exposedThing = new ExposedThing(null);

        final Props props = ThingActor.props(exposedThing);
        final TestActorRef<ThingActor> ref = TestActorRef.create(system, props);

        final CompletableFuture<Object> future = ask(ref, new GetThingDescription(), timeout).toCompletableFuture();

        assertEquals(
                new ThingDescription(ContentManager.valueToContent(exposedThing)),
                future.get()
        );
    }

    @Test
    public void readAllPropertiesShouldBeAnsweredWithProperties() throws ExecutionException, InterruptedException, ContentCodecException {
        when(thing.readProperties()).thenReturn(completedFuture(Map.of("count", 1337)));

        final Props props = ThingActor.props(thing);
        final TestActorRef<ThingActor> ref = TestActorRef.create(system, props);

        final CompletableFuture<Object> future = ask(ref, new ReadAllProperties(), timeout).toCompletableFuture();

        verify(thing).readProperties();
        assertEquals(
                new PropertiesValues(ContentManager.valueToContent(Map.of("count", 1337))),
                future.get()
        );
    }

    @Test
    public void readPropertyShouldBeAnsweredWithReadPropertyDone() throws ExecutionException, InterruptedException, ContentCodecException {
        when(thing.getProperty(any())).thenReturn(property);
        when(property.read()).thenReturn(completedFuture(42));

        final Props props = ThingActor.props(thing);
        final TestActorRef<ThingActor> ref = TestActorRef.create(system, props);

        final CompletableFuture<Object> future = ask(ref, new ReadProperty("count"), timeout).toCompletableFuture();

        verify(property).read();
        assertEquals(
                new ReadPropertyResponse(ContentManager.valueToContent(42)),
                future.get()
        );
    }

    @Test
    public void writePropertyShouldBeAnsweredWithWrittenProperty() throws ExecutionException, InterruptedException, ContentCodecException {
        when(thing.getProperty(any())).thenReturn(property);
        when(property.getClassType()).thenReturn(Object.class);
        when(property.write(any())).thenReturn(completedFuture(42));

        final Props props = ThingActor.props(thing);
        final TestActorRef<ThingActor> ref = TestActorRef.create(system, props);

        final CompletableFuture<Object> future = ask(ref, new WriteProperty("count", ContentManager.valueToContent(1337)), timeout).toCompletableFuture();

        verify(property).write(1337);
        assertEquals(
                new WrittenProperty(ContentManager.valueToContent(42)),
                future.get()
        );
    }

    @Test
    public void subscribePropertyShouldBeAnsweredWithSubscribtionNext() throws ExecutionException, InterruptedException, ContentCodecException {
        when(thing.getProperty(any())).thenReturn(property);
        when(property.observer()).thenReturn(Observable.just(Optional.of(43)));

        final Props props = ThingActor.props(thing);
        final TestActorRef<ThingActor> ref = TestActorRef.create(system, props);

        final CompletableFuture<Object> future = ask(ref, new SubscribeProperty("count"), timeout).toCompletableFuture();

        verify(property).observer();
        assertEquals(
                new Message.SubscriptionNext(ContentManager.valueToContent(43)),
                future.get()
        );
    }

    @Test
    public void subscribeEventShouldBeAnsweredWithSubscribtionNext() throws ExecutionException, InterruptedException, ContentCodecException {
        when(thing.getEvent(any())).thenReturn(event);
        when(event.observer()).thenReturn(Observable.just(Optional.of(43)));

        final Props props = ThingActor.props(thing);
        final TestActorRef<ThingActor> ref = TestActorRef.create(system, props);

        final CompletableFuture<Object> future = ask(ref, new SubscribeEvent("change"), timeout).toCompletableFuture();

        verify(event).observer();
        assertEquals(
                new Message.SubscriptionNext(ContentManager.valueToContent(43)),
                future.get()
        );
    }

    @Test
    public void invokeActionShouldBeAnsweredWithInvokedAction() throws ExecutionException, InterruptedException, ContentCodecException {
        when(thing.getAction(any())).thenReturn(action);
        when(action.invoke(any())).thenReturn(completedFuture(null));

        final Props props = ThingActor.props(thing);
        final TestActorRef<ThingActor> ref = TestActorRef.create(system, props);

        final CompletableFuture<Object> future = ask(ref, new InvokeAction("reset"), timeout).toCompletableFuture();

        verify(action).invoke(null);
        assertEquals(
                new InvokedAction(ContentManager.valueToContent(null)),
                future.get()
        );
    }
}