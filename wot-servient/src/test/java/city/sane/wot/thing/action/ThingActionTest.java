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

import city.sane.wot.thing.schema.DataSchema;
import city.sane.wot.thing.schema.StringSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ThingActionTest {
    private DataSchema input;
    private DataSchema output;

    @BeforeEach
    public void setUp() {
        input = mock(DataSchema.class);
        output = mock(DataSchema.class);
    }

    @Test
    public void builder() {
        ThingAction<Object, Object> action = new ThingAction.Builder()
                .setInput(input)
                .setOutput(output)
                .build();

        assertEquals(input, action.getInput());
        assertEquals(output, action.getOutput());
    }

    @Test
    public void builderShouldUseStringAsDefaultInputSchema() {
        ThingAction<Object, Object> action = new ThingAction.Builder().build();

        assertEquals(new StringSchema(), action.getInput());
    }

    @Test
    public void builderShouldUseStringAsDefaultOutputSchema() {
        ThingAction<Object, Object> action = new ThingAction.Builder().build();

        assertEquals(new StringSchema(), action.getOutput());
    }
}