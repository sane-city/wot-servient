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

import city.sane.wot.thing.schema.DataSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Is responsible for the (de)serialization of arbitrary data structures.
 */
public class ContentManager {
    public static final String DEFAULT = "application/json";
    private static final Logger log = LoggerFactory.getLogger(ContentManager.class);
    private static final Map<String, ContentCodec> CODECS = new HashMap();
    private static final Set<String> OFFERED = new HashSet<>();

    static {
        addCodec(new JsonCodec(), true);
        addCodec(new TextCodec());
        addCodec(new HtmlCodec());
        addCodec(new CborCodec());
        addCodec(new LinkFormatCodec());
    }

    private ContentManager() {
    }

    /**
     * Adds support for media type specified in codec.
     *
     * @param codec
     */
    public static void addCodec(ContentCodec codec) {
        addCodec(codec, false);
    }

    /**
     * Adds support for media type specified in codec. If offered is {@code true}, this media type
     * will also be included in thing descriptions.
     *
     * @param codec
     * @param offered
     */
    private static void addCodec(ContentCodec codec, boolean offered) {
        CODECS.put(codec.getMediaType(), codec);
        if (offered) {
            OFFERED.add(codec.getMediaType());
        }
    }

    /**
     * Removes support for media type specified in codec.
     *
     * @param mediaType
     */
    public static void removeCodec(String mediaType) {
        CODECS.remove(mediaType);
        OFFERED.remove(mediaType);
    }

    /**
     * Servient will offer these media types in the thing descriptions.
     *
     * @return
     */
    public static Set<String> getOfferedMediaTypes() {
        return OFFERED;
    }

    /**
     * Deserializes <code>content</code> according to the data schema defined in
     * <code>schema</code>. Returns <code>null</code> if no schema is specified. If
     * <code>content</code> does not define a content type, {@link #DEFAULT} is assumed. If the
     * content type defined in <code>content</code> is not supported, Java's internal serialization
     * method will be used.
     *
     * @param content
     * @param schema
     * @param <T>
     * @return
     * @throws ContentCodecException
     */
    public static <T> T contentToValue(Content content,
                                       DataSchema<T> schema) throws ContentCodecException {
        if (content == null || content.getBody().length == 0) {
            return null;
        }

        if (schema == null) {
            log.warn("No DataSchema provided. Return null");
            return null;
        }

        String contentType = content.getType();
        if (contentType == null) {
            // default to text/plain
            contentType = DEFAULT;
            log.warn("No content type given. Use default one: {}", DEFAULT);
        }

        String mediaType = getMediaType(contentType);
        Map<String, String> parameters = getMediaTypeParameters(contentType);

        // choose codec based on mediaType
        ContentCodec codec = CODECS.get(mediaType);

        if (codec != null) {
            log.debug("Content deserializing from '{}'", mediaType);
            return codec.bytesToValue(content.getBody(), schema, parameters);
        }
        else {
            log.warn("Content passthrough due to unsupported media type '{}'", mediaType);
            return fallbackBytesToValue(content, schema);
        }
    }

    /**
     * Extracts the media type from <code>contentType</code> (e.g. "text/plain; charset=utf-8"
     * becomes "text/plain"). Returns <code>null</code> if no media type could be found.
     *
     * @param contentType
     * @return
     */
    private static String getMediaType(String contentType) {
        if (contentType == null) {
            return null;
        }

        String[] parts = contentType.split(";", 2);
        return parts[0].trim();
    }

    /**
     * Returns a {@link Map} with all media type parameters in <code>contentType</code> (e.g.
     * "text/plain; charset=utf-8" results in a one-element map with "charset" as key and "utf-8" as
     * value). Returns <code>null</code> if no media type could be found.
     *
     * @param contentType
     * @return
     */
    private static Map<String, String> getMediaTypeParameters(String contentType) {
        if (contentType == null) {
            return null;
        }

        Map<String, String> parameters = new HashMap<>();

        String[] parts = contentType.split(";");
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            int eq = part.indexOf('=');

            String name;
            String value;
            if (eq >= 0) {
                name = part.substring(0, eq).trim();
                value = part.substring(eq + 1).trim();
            }
            else {
                // handle parameters without value
                name = part;
                value = null;
            }

            parameters.put(name, value);
        }

        return parameters;
    }

    private static <T> T fallbackBytesToValue(Content content,
                                              DataSchema<T> schema) throws ContentCodecException {
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(content.getBody());
            ObjectInputStream objectStream = new SecureObjectInputStream(byteStream, schema);

            return (T) objectStream.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            throw new ContentCodecException("Unable to deserialize content: " + e.getMessage());
        }
    }

    /**
     * Serialized <code>value</code> using default content type defined in {@link #DEFAULT} to a
     * {@link Content} object.
     *
     * @param value
     * @return
     * @throws ContentCodecException
     */
    public static Content valueToContent(Object value) throws ContentCodecException {
        return valueToContent(value, null);
    }

    /**
     * Serialized <code>value</code> according to the content type defined in
     * <code>contentType</code> to a {@link Content} object. If the content type defined in
     * <code>contentType</code> is not supported, Java's internal serialization method will be
     * used.
     *
     * @param value
     * @param contentType
     * @return
     * @throws ContentCodecException
     */
    public static Content valueToContent(Object value,
                                         String contentType) throws ContentCodecException {
        if (contentType == null) {
            contentType = ContentManager.DEFAULT;
        }

        byte[] bytes;
        // split into media type and parameters
        String mediaType = getMediaType(contentType);
        Map<String, String> parameters = getMediaTypeParameters(contentType);

        // choose codec based on mediaType
        ContentCodec codec = CODECS.get(mediaType);

        if (codec != null) {
            log.debug("Content serializing to '{}'", mediaType);
            bytes = codec.valueToBytes(value, parameters);
        }
        else {
            log.warn("Content passthrough due to unsupported serialization format '{}'", mediaType);
            bytes = fallbackValueToBytes(value);
        }

        return new Content(contentType, bytes);
    }

    private static byte[] fallbackValueToBytes(Object value) throws ContentCodecException {
        byte[] bytes;
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);

            objectStream.writeObject(value);
            objectStream.flush();

            bytes = byteStream.toByteArray();
        }
        catch (IOException e) {
            throw new ContentCodecException("Unable to serialize content: " + e.getMessage());
        }
        return bytes;
    }

    /**
     * Returns <code>true</code> if data in the format <code>contentType</code> can be
     * (de)serialized. Otherwise <code>false</code> is returned.
     *
     * @param contentType
     * @return
     */
    public static boolean isSupportedMediaType(String contentType) {
        String mediaType = getMediaType(contentType);
        return getSupportedMediaTypes().contains(mediaType);
    }

    /**
     * Servient will accept these media types as payloads for processing requests
     *
     * @return
     */
    public static Set<String> getSupportedMediaTypes() {
        return CODECS.keySet();
    }

    private static class SecureObjectInputStream extends ObjectInputStream {
        private final DataSchema schema;

        public SecureObjectInputStream(InputStream in, DataSchema schema) throws IOException {
            super(in);
            this.schema = schema;
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            if (!isAllowedClass(desc.getName())) {
                throw new InvalidClassException("Disallowed class for DataSchema '" + schema + "': ", desc.getName());
            }
            return super.resolveClass(desc);
        }

        private boolean isAllowedClass(String className) throws ClassNotFoundException {
            return schema.getClassType().isAssignableFrom(Class.forName(className));
        }
    }
}
