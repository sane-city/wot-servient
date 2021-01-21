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
package city.sane.wot.thing.event;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExposedThingEventTest {
    private String name;
    private EventState state;
    private Subject subject;
    private Observer observer;

    @BeforeEach
    public void setUp() {
        name = "change";
        state = mock(EventState.class);
        subject = mock(Subject.class);
        observer = mock(Observer.class);
    }

    @Test
    public void emitWithoutDataShouldEmitNullAsNextValueToEventState() {
        when(state.getSubject()).thenReturn(subject);

        ExposedThingEvent event = new ExposedThingEvent<>(name, state);
        event.emit();

        verify(subject).onNext(Optional.empty());
    }

    @Test
    public void emitShouldEmitGivenDataAsNextValueToEventState() {
        when(state.getSubject()).thenReturn(subject);

        ExposedThingEvent event = new ExposedThingEvent<>(name, state);
        event.emit("Hallo Welt");

        verify(subject).onNext(Optional.of("Hallo Welt"));
    }

    @Test
    public void subscribeShouldSubscribeToEventState() {
        when(state.getSubject()).thenReturn(subject);

        ExposedThingEvent event = new ExposedThingEvent<>(name, state);
        event.observer().subscribe(observer);

        verify(subject).subscribe(observer);
    }
}