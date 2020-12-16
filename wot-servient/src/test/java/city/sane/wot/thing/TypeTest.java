package city.sane.wot.thing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TypeTest {
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void fromJson() throws IOException {
        // single value
        assertEquals(
                new Type("Thing"),
                jsonMapper.readValue("\"Thing\"", Type.class)
        );

        // array
        assertEquals(
                new Type("Thing").addType("saref:LightSwitch"),
                jsonMapper.readValue("[\"Thing\",\"saref:LightSwitch\"]", Type.class)
        );
    }

    @Test
    public void toJson() throws JsonProcessingException {
        // single value
        assertEquals(
                "\"Thing\"",
                jsonMapper.writeValueAsString(new Type("Thing"))
        );

        // multi type array
        assertThatJson(jsonMapper.writeValueAsString(new Type("Thing").addType("saref:LightSwitch")))
                .when(Option.IGNORING_ARRAY_ORDER)
                .isArray()
                .contains("Thing", "saref:LightSwitch");
    }
}