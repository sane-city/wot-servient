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

import city.sane.wot.thing.schema.NumberSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import city.sane.wot.thing.schema.StringSchema;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentManagerTest {
    @Test
    public void contentToValueShouldUseFallbackDeserializerForUnsupportedFormats() throws ContentCodecException, IOException {
        // serialize 42
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(42);
        oos.flush();
        byte[] body = bos.toByteArray();

        Content content = new Content("none/none", body);
        Object value = ContentManager.contentToValue(content, new NumberSchema());

        assertEquals(42, value);
    }

    @Test
    public void contentToValueObject() throws ContentCodecException {
        Content content = new Content("application/json", "{\"foo\":\"bar\"}".getBytes());
        Map value = ContentManager.contentToValue(content, new ObjectSchema());

        assertEquals("bar", value.get("foo"));
    }

    @Test
    public void contentToValueWithMediaTypeParameters() throws ContentCodecException {
        Content content = new Content("text/plain; charset=utf-8", "Hello World".getBytes());
        String value = ContentManager.contentToValue(content, new StringSchema());

        assertEquals("Hello World", value);
    }

    @Test
    public void contentToValueBrokenByteArray() {
        Content content = new Content("application/xml", new byte[]{ 0x4f });

        assertThrows(ContentCodecException.class, () -> ContentManager.contentToValue(content, new StringSchema()));
    }

    @Test
    public void valueToContentWithUnsupportedFormat() throws ContentCodecException, IOException, ClassNotFoundException {
        Content content = ContentManager.valueToContent(42, "none/none");

        // deserialize byte array
        ByteArrayInputStream bis = new ByteArrayInputStream(content.getBody());
        ObjectInputStream ois = new ObjectInputStream(bis);

        assertEquals(42, ois.readObject());
    }

    @Test
    public void removeCodec() {
        try {
            assertTrue(ContentManager.isSupportedMediaType("text/plain"));
            ContentManager.removeCodec("text/plain");
            assertFalse(ContentManager.isSupportedMediaType("text/plain"));
        }
        finally {
            ContentManager.addCodec(new TextCodec());
        }
    }
}