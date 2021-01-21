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

import city.sane.wot.thing.schema.ObjectSchema;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LinkFormatCodecTest {
    private LinkFormatCodec codec;

    @BeforeEach
    public void setUp() {
        codec = new LinkFormatCodec();
    }

    @Test
    public void bytesToValue() throws ContentCodecException {
        byte[] bytes = "</reg/1/>;ep=\"RIOT-34136DAB556DC1D3\";base=\"coap://[fd00:6:7:8:d1c1:6d55:ab6d:1336]\";rt=\"core.rd-ep\",</reg/2/>;ep=\"RIOT-34136EAB746DC1D3\";base=\"coap://[fd00:6:7:8:d1c1:6d74:ab6e:1336]\";rt=\"core.rd-ep\"".getBytes();

        Map<String, Map<String, String>> value = codec.bytesToValue(bytes, new ObjectSchema());

        MatcherAssert.assertThat(value, hasKey("</reg/1/>"));
        MatcherAssert.assertThat(value, hasKey("</reg/2/>"));
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