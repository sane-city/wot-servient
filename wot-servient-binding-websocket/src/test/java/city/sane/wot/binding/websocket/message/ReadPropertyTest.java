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
package city.sane.wot.binding.websocket.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReadPropertyTest {
    private ReadProperty message;

    @BeforeEach
    public void setUp() {
        message = new ReadProperty("counter", "count");
    }

    @Test
    public void testConstructorNullParams() {
        assertThrows(NullPointerException.class, () -> new ReadProperty(null, null));
        assertThrows(NullPointerException.class, () -> new ReadProperty("counter", null));
        assertThrows(NullPointerException.class, () -> new ReadProperty(null, "count"));
    }

    @Test
    public void getThingId() {
        assertEquals("counter", message.getThingId());
    }

    @Test
    public void getName() {
        assertEquals("count", message.getName());
    }
}