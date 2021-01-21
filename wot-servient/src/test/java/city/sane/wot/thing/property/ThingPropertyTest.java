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
package city.sane.wot.thing.property;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThingPropertyTest {
    @Test
    public void testEquals() {
        ThingProperty<Object> property1 = new ThingProperty<Object>();
        ThingProperty<Object> property2 = new ThingProperty<Object>();

        assertEquals(property1, property2);
    }

    @Test
    public void builder() {
        ThingProperty<Object> property = new ThingProperty.Builder()
                .setObjectType("saref:Temperature")
                .setType("integer")
                .setObservable(true)
                .setReadOnly(true)
                .setWriteOnly(false)
                .setOptionalProperties(Map.of("foo", "bar"))
                .build();

        assertEquals("saref:Temperature", property.getObjectType());
        assertEquals("integer", property.getType());
        assertTrue(property.isObservable());
        assertTrue(property.isReadOnly());
        assertFalse(property.isWriteOnly());
        assertEquals(Map.of("foo", "bar"), property.getOptionalProperties());
        assertEquals("bar", property.getOptional("foo"));
    }

    @Test
    public void builderShouldUseStringAsDefaultType() {
        ThingProperty<Object> property = new ThingProperty.Builder().build();

        assertEquals("string", property.getType());
        assertEquals(String.class, property.getClassType());
    }
}