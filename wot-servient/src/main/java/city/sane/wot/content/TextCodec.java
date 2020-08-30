package city.sane.wot.content;

import city.sane.wot.thing.schema.BooleanSchema;
import city.sane.wot.thing.schema.DataSchema;
import city.sane.wot.thing.schema.IntegerSchema;
import city.sane.wot.thing.schema.NumberSchema;
import city.sane.wot.thing.schema.StringSchema;

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
    public <T> T bytesToValue(byte[] body, DataSchema<T> schema, Map<String, String> parameters) {
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
        switch (type) {
            case BooleanSchema
                    .TYPE:
                return (T) Boolean.valueOf(parsed);
            case IntegerSchema
                    .TYPE:
                return (T) Integer.valueOf(parsed);
            case NumberSchema
                    .TYPE:
                if (parsed.contains(".")) {
                    return (T) Double.valueOf(parsed);
                }
                else {
                    return (T) Long.valueOf(parsed);
                }
            case StringSchema
                    .TYPE:
                return (T) parsed;
            default:
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
