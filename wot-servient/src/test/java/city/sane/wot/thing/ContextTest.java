package city.sane.wot.thing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ContextTest {
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void fromJson() throws IOException {
        // single value
        assertEquals(
                new Context("http://www.w3.org/ns/td"),
                jsonMapper.readValue("\"http://www.w3.org/ns/td\"", Context.class)
        );

        // array
        assertEquals(
                new Context("http://www.w3.org/ns/td"),
                jsonMapper.readValue("[\"http://www.w3.org/ns/td\"]", Context.class)
        );

        // multi type array
        assertEquals(
                new Context("http://www.w3.org/ns/td").addContext("saref", "https://w3id.org/saref#"),
                jsonMapper.readValue("[\"http://www.w3.org/ns/td\",{\"saref\":\"https://w3id.org/saref#\"}]", Context.class)
        );
    }

    @Test
    public void toJson() throws JsonProcessingException {
        // single value
        assertEquals(
                "\"http://www.w3.org/ns/td\"",
                jsonMapper.writeValueAsString(new Context("http://www.w3.org/ns/td"))
        );

        // multi type array
        assertEquals(
                "[\"http://www.w3.org/ns/td\",{\"saref\":\"https://w3id.org/saref#\"}]",
                jsonMapper.writeValueAsString(new Context("http://www.w3.org/ns/td").addContext("saref", "https://w3id.org/saref#"))
        );
    }
}