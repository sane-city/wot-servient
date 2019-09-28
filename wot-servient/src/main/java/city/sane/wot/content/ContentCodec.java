package city.sane.wot.content;

import city.sane.wot.thing.schema.DataSchema;

import java.util.Collections;
import java.util.Map;

/**
 * A ContentCodec is responsible for (de)serializing data in certain encoding (e.g. JSON, CBOR).
 */
public interface ContentCodec {
    String getMediaType();

    /**
     * Deserializes <code>body</code> according to the data schema defined in <code>schema</code>. <code>parameters</code> can contain additional information
     * about the encoding of the data (e.g. the used character set).
     *
     * @param body
     * @param schema
     * @param parameters
     * @param <T>
     *
     * @return
     * @throws ContentCodecException
     */
    <T> T bytesToValue(byte[] body, DataSchema<T> schema, Map<String, String> parameters) throws ContentCodecException;

    /**
     * Deserializes <code>body</code> according to the data schema defined in <code>schema</code>.
     *
     * @param body
     * @param schema
     * @param <T>
     *
     * @return
     * @throws ContentCodecException
     */
    default <T> T bytesToValue(byte[] body, DataSchema<T> schema) throws ContentCodecException {
        return bytesToValue(body, schema, Collections.emptyMap());
    }

    /**
     * Serialized <code>value</code> according to the data schema defined in <code>schema</code> to a byte array. <code>parameters</code> can contain additional
     * information about the encoding of the data (e.g. the used character set).
     *
     * @param value
     * @param parameters
     *
     * @return
     * @throws ContentCodecException
     */
    byte[] valueToBytes(Object value, Map<String, String> parameters) throws ContentCodecException;

    /**
     * Serialized <code>value</code> according to the data schema defined in <code>schema</code> to a byte array.
     *
     * @param value
     *
     * @return
     * @throws ContentCodecException
     */
    default byte[] valueToBytes(Object value) throws ContentCodecException {
        return valueToBytes(value, Collections.emptyMap());
    }
}
