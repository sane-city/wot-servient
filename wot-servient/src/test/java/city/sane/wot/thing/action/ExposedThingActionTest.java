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
package city.sane.wot.thing.action;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.schema.DataSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExposedThingActionTest {
    private ExposedThing thing;
    private ActionState<Object, Object> state;
    private String description;
    private DataSchema input;
    private DataSchema output;
    private BiFunction handler;
    private Object invokeInput;
    private Map<String, Map<String, Object>> invokeOptions;

    @BeforeEach
    public void setUp() {
        thing = mock(ExposedThing.class);
        state = mock(ActionState.class);
        description = "";
        input = mock(DataSchema.class);
        output = mock(DataSchema.class);
        handler = mock(BiFunction.class);
        invokeInput = mock(Object.class);
        invokeOptions = mock(Map.class);
    }

    @Test
    public void invokeWithoutHandlerShouldReturnNullFuture() throws ExecutionException, InterruptedException {
        ExposedThingAction<Object, Object> exposedThingAction = new ExposedThingAction<Object, Object>("myAction", thing, state, description, Map.of(), Map.of(), input, output);
        exposedThingAction.invoke(invokeInput, invokeOptions);

        assertNull(exposedThingAction.invoke(invokeInput, invokeOptions).get());
    }

    @Test
    public void invokeWithHandlerShouldCallHandler() {
        when(state.getHandler()).thenReturn(handler);

        ExposedThingAction<Object, Object> exposedThingAction = new ExposedThingAction<Object, Object>("myAction", thing, state, description, Map.of(), Map.of(), input, output);
        exposedThingAction.invoke(invokeInput, invokeOptions);

        verify(handler).apply(invokeInput, invokeOptions);
    }

    @Test
    public void invokeWithBrokenHandlerShouldReturnFailedFuture() {
        when(handler.apply(any(), any())).thenThrow(new RuntimeException());
        when(state.getHandler()).thenReturn(handler);

        ExposedThingAction<Object, Object> exposedThingAction = new ExposedThingAction<Object, Object>("myAction", thing, state, description, Map.of(), Map.of(), input, output);
        assertThrows(ExecutionException.class, () -> exposedThingAction.invoke(invokeInput, invokeOptions).get());

        verify(handler).apply(invokeInput, invokeOptions);
    }

    @Test
    public void invokeShouldWrapNullResultsFromHandlerIntoFuture() {
        when(state.getHandler()).thenReturn(handler);
        when(handler.apply(any(), any())).thenReturn(null);

        ExposedThingAction<Object, Object> exposedThingAction = new ExposedThingAction<Object, Object>("myAction", thing, state, description, Map.of(), Map.of(), input, output);

        assertNotNull(exposedThingAction.invoke());
    }
}