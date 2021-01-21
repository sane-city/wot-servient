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

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import city.sane.wot.binding.akka.actor.DiscoverActor.Discovered;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.ThingFilter;
import io.reactivex.rxjava3.core.Observer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;

import static akka.pattern.Patterns.ask;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DiscoverActorTest {
    private ActorSystem system;
    private Observer<Thing> observer;
    private ThingFilter filter;
    private Duration timeout;

    @BeforeEach
    public void setUp() {
        system = ActorSystem.create();
        observer = mock(Observer.class);
        filter = mock(ThingFilter.class);
        timeout = Duration.ofSeconds(5);
    }

    @AfterEach
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void discoveredShouldAddThingsToObserver() {
        final Props props = DiscoverActor.props(observer, filter, timeout);
        final TestActorRef<DiscoverActor> ref = TestActorRef.create(system, props);

        ExposedThing exposedThing = new ExposedThing(null);
        Map<String, ExposedThing> things = Map.of("counter", exposedThing);
        ask(ref, new Discovered(things), timeout).toCompletableFuture();

        verify(observer).onNext(any());
    }
}