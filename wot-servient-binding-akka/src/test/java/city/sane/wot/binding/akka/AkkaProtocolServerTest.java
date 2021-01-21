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
import city.sane.wot.binding.akka.actor.ThingsActor;
import city.sane.wot.thing.ExposedThing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AkkaProtocolServerTest {
    private ExposedThing thing;
    private RefCountResource<Triple<ActorSystem, Map<String, ExposedThing>, ActorRef>> actorSystemProvider;
    private AkkaProtocolPattern pattern;
    private Triple<ActorSystem, Map<String, ExposedThing>, ActorRef> triple;
    private Map<String, ExposedThing> things;
    private ActorRef thingsActor;
    private CompletionStage completionStage;

    @BeforeEach
    public void setUp() {
        actorSystemProvider = mock(RefCountResource.class);
        pattern = mock(AkkaProtocolPattern.class);
        triple = mock(Triple.class);
        things = mock(Map.class);
        thingsActor = mock(ActorRef.class);
        thing = mock(ExposedThing.class);
        completionStage = mock(CompletionStage.class);
    }

    @Test
    public void exposeShouldSendExposeMessageToThingsActor() {
        when(triple.second()).thenReturn(things);
        when(triple.third()).thenReturn(thingsActor);
        when(pattern.ask(any(ActorRef.class), any(), any())).thenReturn(completionStage);
        when(completionStage.thenRun(any())).thenReturn(completionStage);

        AkkaProtocolServer server = new AkkaProtocolServer(actorSystemProvider, pattern, triple);
        server.expose(thing);

        verify(pattern, timeout(1 * 1000L)).ask(eq(thingsActor), any(ThingsActor.Expose.class), any());
    }
}