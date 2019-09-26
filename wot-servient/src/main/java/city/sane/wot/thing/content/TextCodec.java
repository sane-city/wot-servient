package city.sane.wot.thing.content;

import city.sane.wot.thing.schema.DataSchema;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * (De)serializes data in plaintext format.
 */
public class TextCodec implements ContentCodec {
    @Override
    public String getMediaType() {
        return "text/plain";
    }

    @Override
    public <T> T bytesToValue(byte[] body, DataSchema<T> schema, Map<String, String> parameters) throws ContentCodecException {
        String charset = parameters.get("charset");

        String parsed;
        if (charset != null) {
            parsed = new String(body, Charset.forName(charset));
        }
        else {
            parsed = new String(body);
        }

        String type = schema.getType();
        // TODO: array, object
        if (type.equals("boolean")) {
            return (T) Boolean.valueOf(parsed);
        }
        else if (type.equals("integer")) {
            return (T) Integer.valueOf(parsed);
        }
        else if (type.equals("number")) {
            if (parsed.contains(".")) {
                return (T) Double.valueOf(parsed);
            }
            else {
                return (T) Long.valueOf(parsed);
            }
        }
        else if (type.equals("string")) {
            return (T) parsed;
        }
        else {
            return null;
        }
    }

    @Override
    public byte[] valueToBytes(Object value, Map<String, String> parameters) {
        String charset = parameters.get("charset");

        if (charset != null) {
            return value.toString().getBytes(Charset.forName(charset));
        }
        else {
            return value.toString().getBytes();
        }
    }
}
