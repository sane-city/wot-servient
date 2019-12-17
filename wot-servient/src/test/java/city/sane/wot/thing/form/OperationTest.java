package city.sane.wot.thing.form;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class OperationTest {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Test
    public void toJson() throws JsonProcessingException {
        Operation op = Operation.READ_PROPERTY;
        String json = JSON_MAPPER.writeValueAsString(op);

        assertEquals("\"readproperty\"", json);
    }

    @Test
    public void fromJson() throws IOException {
        String json = "\"writeproperty\"";
        Operation op = JSON_MAPPER.readValue(json, Operation.class);

        assertEquals(Operation.WRITE_PROPERTY, op);
    }
}