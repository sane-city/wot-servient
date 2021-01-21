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
package city.sane.wot.binding.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import city.sane.RefCountResource;
import city.sane.Triple;
import city.sane.wot.thing.ExposedThing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class AkkaProtocolClientFactoryTest {
    private RefCountResource<Triple<ActorSystem, Map<String, ExposedThing>, ActorRef>> actorSystemProvider;
    private Duration askTimeout;
    private Duration discoverTimeout;
    private Triple<ActorSystem, Map<String, ExposedThing>, ActorRef> triple;

    @BeforeEach
    public void setUp() {
        actorSystemProvider = mock(RefCountResource.class);
        askTimeout = Duration.ofSeconds(60);
        discoverTimeout = Duration.ofSeconds(5);
        triple = mock(Triple.class);
    }

    @Test
    public void getSchemeShouldReturnCorrectScheme() {
        AkkaProtocolClientFactory factory = new AkkaProtocolClientFactory(actorSystemProvider, askTimeout, discoverTimeout, triple);
        assertEquals("akka", factory.getScheme());
    }

    @Test
    public void getClientShouldReturnCorrectClient() {
        AkkaProtocolClientFactory factory = new AkkaProtocolClientFactory(actorSystemProvider, askTimeout, discoverTimeout, triple);
        factory.init();

        assertThat(factory.getClient(), instanceOf(AkkaProtocolClient.class));
    }
}
