/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import city.sane.wot.binding.akka.actor.DiscoverActor.Discover;
import city.sane.wot.binding.akka.actor.DiscoverActor.Discovered;
import city.sane.wot.binding.akka.actor.ThingsActor.Destroy;
import city.sane.wot.binding.akka.actor.ThingsActor.Destroyed;
import city.sane.wot.binding.akka.actor.ThingsActor.Expose;
import city.sane.wot.binding.akka.actor.ThingsActor.Exposed;
import city.sane.wot.binding.akka.actor.ThingsActor.GetThings;
import city.sane.wot.binding.akka.actor.ThingsActor.Things;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.filter.ThingFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static akka.actor.ActorRef.noSender;
import static akka.pattern.Patterns.ask;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//@ExtendWith(MockitoExtension.class)
public class ThingsActorTest {
    private ActorSystem system;
    private Map<String, ExposedThing> things;
    private Map<String, ActorRef> thingActors;
    private Map<String, ActorRef> exposeRequesters = new HashMap<>();
    private Map<String, ActorRef> destroyRequesters = new HashMap<>();
    private ExposedThing thing;
    private BiFunction<AbstractActor.ActorContext, ExposedThing, ActorRef> thingActorCreator;
    private BiConsumer<AbstractActor.ActorContext, ActorRef> thingActorDestroyer;
    private ActorRef actorRef;
    private Duration timeout;
    private ThingFilter filter;

    @BeforeEach
    public void setUp() {
        system = ActorSystem.create();
        things = mock(Map.class);
        thingActors = mock(Map.class);
        exposeRequesters = mock(Map.class);
        destroyRequesters = mock(Map.class);
        thingActorCreator = mock(BiFunction.class);
        thingActorDestroyer = mock(BiConsumer.class);
        thing = mock(ExposedThing.class);
        actorRef = mock(ActorRef.class);
        timeout = Duration.ofMillis(3000);
        filter = mock(ThingFilter.class);
    }

    @AfterEach
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void getThingsShouldBeAnsweredWithThings() throws ExecutionException, InterruptedException, ContentCodecException {
        ExposedThing exposedThing = new ExposedThing(null);

        final Props props = ThingsActor.props(Map.of("counter", exposedThing), thingActors, exposeRequesters, destroyRequesters, thingActorCreator, thingActorDestroyer);
        final TestActorRef<ThingActor> ref = TestActorRef.create(system, props);

        final CompletableFuture<Object> future = ask(ref, new GetThings(), timeout).toCompletableFuture();

        assertEquals(
                new Things(ContentManager.valueToContent(Map.of("counter", exposedThing))),
                future.get()
        );
    }

    @Test
    public void exposeShouldCreatedThingActor() {
        when(things.get(any())).thenReturn(thing);

        final Props props = ThingsActor.props(things, thingActors, exposeRequesters, destroyRequesters, thingActorCreator, thingActorDestroyer);
        final TestActorRef<ThingActor> ref = TestActorRef.create(system, props);

        ref.tell(new Expose("counter"), noSender());

        verify(thingActorCreator).apply(any(), eq(thing));
    }

    @Test
    public void exposedShouldRedirectMessageToRequester() {
        when(things.get(any())).thenReturn(thing);
        when(exposeRequesters.remove(any())).thenReturn(actorRef);

        final Props props = ThingsActor.props(things, thingActors, exposeRequesters, destroyRequesters, thingActorCreator, thingActorDestroyer);
        final TestActorRef<ThingActor> ref = TestActorRef.create(system, props);

        ref.tell(new Exposed("counter"), noSender());

        verify(exposeRequesters).remove("counter");
//        verify(actorRef).tell(any(), any());
    }

    @Test
    public void destroyShouldStopThingActor() {
        when(thingActors.remove(any())).thenReturn(actorRef);

        final Props props = ThingsActor.props(things, thingActors, exposeRequesters, destroyRequesters, thingActorCreator, thingActorDestroyer);
        final TestActorRef<ThingActor> ref = TestActorRef.create(system, props);

        ref.tell(new Destroy("counter"), noSender());

        verify(thingActorDestroyer).accept(any(), eq(actorRef));
    }

    @Test
    public void destroyedShouldRedirectMessageToRequester() {
        when(things.get(any())).thenReturn(thing);
        when(destroyRequesters.remove(any())).thenReturn(actorRef);

        final Props props = ThingsActor.props(things, thingActors, exposeRequesters, destroyRequesters, thingActorCreator, thingActorDestroyer);
        final TestActorRef<ThingActor> ref = TestActorRef.create(system, props);

        ref.tell(new Destroyed("counter"), noSender());

        verify(destroyRequesters).remove("counter");
//        verify(actorRef).tell(any(), any());
    }

    @Test
    public void discoverShouldBeAnsweredWithThings() throws ExecutionException, InterruptedException {
        when(thing.getId()).thenReturn("counter");

        final Props props = ThingsActor.props(Map.of("counter", thing), thingActors, exposeRequesters, destroyRequesters, thingActorCreator, thingActorDestroyer);
        final TestActorRef<ThingActor> ref = TestActorRef.create(system, props);

        final CompletableFuture<Object> future = ask(ref, new Discover(filter), timeout).toCompletableFuture();

        assertEquals(
                new Discovered(Map.of("counter", thing)),
                future.get()
        );
    }

    // TODO: exposed
    // TODO: destroyed
}
