package city.sane.wot.content;

import city.sane.wot.thing.schema.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TextCodecTest {
    private TextCodec codec;

    @Before
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

        assertNotNull("Should be instance of Number", value);
        Number number = value;

        assertEquals(13.37, number);
    }

    @Test
    public void bytesToNumberLongValue() throws ContentCodecException {
        byte[] bytes = "1337".getBytes();
        Number value = codec.bytesToValue(bytes, new NumberSchema());

        assertNotNull("Should be instance of Number", value);
        Number number = value;

        assertEquals(1337L, number);
    }

    @Test
    public void bytesToStringValue() throws ContentCodecException {
        byte[] bytes = "Hallo Welt".getBytes();
        String value = codec.bytesToValue(bytes, new StringSchema());

        assertNotNull("Should be instance of String", value);
        String string = value;

        assertEquals("Hallo Welt", string);
    }

    @Test
    public void valueToBytes() throws ContentCodecException {
        String value = "Hallo Welt";
        byte[] bytes = codec.valueToBytes(value);

        assertArrayEquals("Hallo Welt".getBytes(), bytes);
    }
}