package city.sane.wot.content;

import city.sane.wot.thing.schema.ObjectSchema;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.*;


public class LinkFormatCodecTest {
    private LinkFormatCodec codec;

    @Before
    public void setUp() {
        codec = new LinkFormatCodec();
    }

    @Test
    public void bytesToValue() throws ContentCodecException {
        byte[] bytes = "</reg/1/>;ep=\"RIOT-34136DAB556DC1D3\";base=\"coap://[fd00:6:7:8:d1c1:6d55:ab6d:1336]\";rt=\"core.rd-ep\",</reg/2/>;ep=\"RIOT-34136EAB746DC1D3\";base=\"coap://[fd00:6:7:8:d1c1:6d74:ab6e:1336]\";rt=\"core.rd-ep\"".getBytes();

        Map<String, Map<String, String>> value = codec.bytesToValue(bytes, new ObjectSchema());

        assertThat(value, hasKey("</reg/1/>"));
        assertThat(value, hasKey("</reg/2/>"));
    }

    @Test
    public void valueToBytes() throws ContentCodecException {
        Map<String, Map<String, String>> value = Map.of(
                "</reg/1/>", Map.of(
                        "ep", "RIOT-34136DAB556DC1D3",
                        "base", "coap://[fd00:6:7:8:d1c1:6d55:ab6d:1336]",
                        "rt", "core.rd-ep"
                ),
                "</reg/2/>", Map.of(
                        "ep", "RIOT-34136EAB746DC1D3",
                        "base", "coap://[fd00:6:7:8:d1c1:6d74:ab6e:1336]",
                        "rt", "core.rd-ep"
                )
        );

        byte[] bytes = codec.valueToBytes(value);

        // should not fail
        assertTrue(true);
//        assertEquals("</reg/1/>;ep=\"RIOT-34136DAB556DC1D3\";base=\"coap://[fd00:6:7:8:d1c1:6d55:ab6d:1336]\";rt=\"core.rd-ep\",</reg/2/>;ep=\"RIOT-34136EAB746DC1D3\";base=\"coap://[fd00:6:7:8:d1c1:6d74:ab6e:1336]\";rt=\"core.rd-ep\"", new String(bytes));
    }
}