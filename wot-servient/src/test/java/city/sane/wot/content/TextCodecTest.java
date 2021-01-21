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

import city.sane.wot.thing.schema.BooleanSchema;
import city.sane.wot.thing.schema.IntegerSchema;
import city.sane.wot.thing.schema.NullSchema;
import city.sane.wot.thing.schema.NumberSchema;
import city.sane.wot.thing.schema.StringSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextCodecTest {
    private TextCodec codec;

    @BeforeEach
    public void setUp() {
        codec = new TextCodec();
    }

    @Test
    public void bytesToBooleanValue() throws ContentCodecException {
        byte[] bytes = "true".getBytes();
        Object value = codec.bytesToValue(bytes, new BooleanSchema());

        boolean bool = (boolean) value;

        assertTrue(bool);
    }

    @Test
    public void bytesToIntegerValue() throws ContentCodecException {
        byte[] bytes = "1337".getBytes();
        Object value = codec.bytesToValue(bytes, new IntegerSchema());

        int integer = (int) value;

        assertEquals(1337, integer);
    }

    @Test
    public void bytesToNullValue() throws ContentCodecException {
        byte[] bytes = "null".getBytes();
        Object value = codec.bytesToValue(bytes, new NullSchema());

        assertNull(value);
    }

    @Test
    public void bytesToNumberFloatValue() throws ContentCodecException {
        byte[] bytes = "13.37".getBytes();
        Number value = codec.bytesToValue(bytes, new NumberSchema());

        assertNotNull(value, "Should be instance of Number");

        assertEquals(13.37, value);
    }

    @Test
    public void bytesToNumberLongValue() throws ContentCodecException {
        byte[] bytes = "1337".getBytes();
        Number value = codec.bytesToValue(bytes, new NumberSchema());

        assertNotNull(value, "Should be instance of Number");

        assertEquals(1337L, value);
    }

    @Test
    public void bytesToStringValue() throws ContentCodecException {
        byte[] bytes = "Hallo Welt".getBytes();
        String value = codec.bytesToValue(bytes, new StringSchema());

        assertNotNull(value, "Should be instance of String");

        assertEquals("Hallo Welt", value);
    }

    @Test
    public void valueToBytes() throws ContentCodecException {
        String value = "Hallo Welt";
        byte[] bytes = codec.valueToBytes(value);

        assertArrayEquals("Hallo Welt".getBytes(), bytes);
    }
}