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
package city.sane.wot.thing.form;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormTest {
    @Test
    public void builder() {
        Form form = new Form.Builder()
                .setHref("test:/foo")
                .setOp(Operation.OBSERVE_PROPERTY)
                .setSubprotocol("longpolling")
                .setContentType("application/json")
                .setOptionalProperties(Map.of("foo", "bar"))
                .build();

        assertEquals("test:/foo", form.getHref());
        assertEquals(Collections.singletonList(Operation.OBSERVE_PROPERTY), form.getOp());
        assertEquals("longpolling", form.getSubprotocol());
        assertEquals("application/json", form.getContentType());
        assertEquals(Map.of("foo", "bar"), form.getOptionalProperties());
        assertEquals("bar", form.getOptional("foo"));
    }
}