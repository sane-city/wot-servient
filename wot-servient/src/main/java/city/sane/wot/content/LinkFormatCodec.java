package city.sane.wot.content;

import city.sane.wot.thing.schema.DataSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

public class LinkFormatCodec implements ContentCodec {
    private final Logger log = LoggerFactory.getLogger(LinkFormatCodec.class);

    @Override
    public String getMediaType() {
        return "application/link-format";
    }

    @Override
    public <T> T bytesToValue(byte[] body, DataSchema<T> schema, Map<String, String> parameters) throws ContentCodecException {
        Pattern pattern = Pattern.compile("^(.+)=\"(.+)\"");
        if (schema.getType().equals("object")) {
            Map<String, Map<String, String>> entries = new HashMap<>();

            String bodyString = new String(body);
            String[] entriesString = bodyString.split(",");
            for (String entryString : entriesString) {
                List<String> entryComponentsString = new ArrayList(Arrays.asList(entryString.split(";", 4)));
                String entryKey = entryComponentsString.remove(0);

                Map<String, String> entryParameters = new HashMap<>();
                entryComponentsString.forEach(s -> {
                    Matcher matcher = pattern.matcher(s);
                    if (matcher.find()) {
                        String key = matcher.group(1);
                        String value = matcher.group(2);
                        entryParameters.put(key, value);
                    }
                });

                entries.put(entryKey, entryParameters);
            }
            return (T) entries;
        }
        else {
            throw new ContentCodecException("Non-object data schema not implemented yet");
        }
    }

    @Override
    public byte[] valueToBytes(Object value, Map<String, String> parameters) {
        if (value instanceof Map) {
            Map<String, Map<String, String>> valueMap = (Map<String, Map<String, String>>) value;
            String bodyString = valueMap.entrySet().stream().map(e -> e.getKey() + ";" + e.getValue().entrySet().stream().map(e2 -> e2.getKey() + "=\"" + e2.getValue() + "\"").collect(joining(";"))).collect(joining(","));
            return bodyString.getBytes();
        }
        else {
            log.warn("Unable to serialize non-map value: {}", value);
            return new byte[0];
        }
    }
}
