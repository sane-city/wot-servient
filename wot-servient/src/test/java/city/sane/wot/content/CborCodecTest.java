package city.sane.wot.content;

import city.sane.wot.thing.schema.StringSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

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