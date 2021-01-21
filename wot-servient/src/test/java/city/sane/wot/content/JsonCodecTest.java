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