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

import city.sane.wot.thing.schema.ObjectSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonCodecTest {
    private JsonCodec codec;

    @BeforeEach
    public void setUp() {
        codec = new JsonCodec();
    }

    @Test
    public void bytesToValue() throws ContentCodecException {
        byte[] bytes = "{\"foo\":\"bar\"}".getBytes();
        Map value = codec.bytesToValue(bytes, new ObjectSchema());

        assertEquals("bar", value.get("foo"));
    }

    @Test
    public void valueToBytes() throws ContentCodecException {
        List<String> value = Arrays.asList("foo", "bar");
        byte[] bytes = codec.valueToBytes(value);

        assertEquals("[\"foo\",\"bar\"]", new String(bytes));
    }

    @Test
    public void mapToJsonToMap() throws ContentCodecException {
        Map<String, Object> value = Map.of(
                "foo", "bar",
                "etzala", Map.of("hello", "world")
        );
        byte[] bytes = codec.valueToBytes(value);
        Object newValue = codec.bytesToValue(bytes, new ObjectSchema());

        assertThat(newValue, instanceOf(Map.class));
    }
}