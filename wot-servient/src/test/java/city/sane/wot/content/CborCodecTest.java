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
package city.sane.wot.content;

import city.sane.wot.thing.schema.StringSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CborCodecTest {
    private CborCodec codec;

    @BeforeEach
    public void setUp() {
        codec = new CborCodec();
    }

    @Test
    public void bytesToValue() throws ContentCodecException {
        byte[] bytes = "jHallo Welt".getBytes();

        Object value = codec.bytesToValue(bytes, new StringSchema());

        assertEquals("Hallo Welt", value);
    }

    @Test
    public void valueToBytes() throws ContentCodecException {
        String value = "Hallo Welt";
        byte[] bytes = codec.valueToBytes(value);

        assertEquals("jHallo Welt", new String(bytes));
    }
}