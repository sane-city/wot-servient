package city.sane.wot.thing;

import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.property.ThingProperty;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ThingTest {
    @Test
    public void toJson() {
        Thing thing = new Thing.Builder()
                .setId("Foo")
                .setDescription("Bar")
                .setObjectType("Thing")
                .setObjectContext(new Context("http://www.w3.org/ns/td"))
                .build();

        assertEquals(
                "{\"id\":\"Foo\",\"description\":\"Bar\",\"@type\":\"Thing\",\"@context\":\"http://www.w3.org/ns/td\"}",
                thing.toJson()
        );
    }

    @Test
    public void fromJson() {
        String json = "{\"id\":\"Foo\",\"description\":\"Bar\",\"@type\":\"Thing\",\"@context\":[\"http://www.w3.org/ns/td\"]}";

        Thing thing = Thing.fromJson(json);

        assertEquals("Foo", thing.getId());
        assertEquals("Bar", thing.getDescription());
        assertEquals("Thing", thing.getObjectType());
        assertEquals(new Context("http://www.w3.org/ns/td"), thing.getObjectContext());
    }

    @Test
    public void getPropertiesByObjectType() {
        String json = "{\n" +
                "  \"id\" : \"KlimabotschafterWetterstationen:Ahrensburg\",\n" +
                "  \"title\" : \"KlimabotschafterWetterstationen:Ahrensburg\",\n" +
                "  \"properties\" : {\n" +
                "    \"Hum_2m\" : {\n" +
                "      \"description\" : \"Relative Luftfeuchtigkeit 2 m in %\",\n" +
                "      \"type\" : \"number\",\n" +
                "      \"observable\" : true,\n" +
                "      \"readOnly\" : true,\n" +
                "      \"classType\" : \"java.lang.Object\",\n" +
                "      \"@type\" : \"saref:Humidity\"\n" +
                "    },\n" +
                "    \"Temp_2m\" : {\n" +
                "      \"description\" : \"Temperatur in 2m in Grad Celsisus\",\n" +
                "      \"type\" : \"number\",\n" +
                "      \"observable\" : true,\n" +
                "      \"readOnly\" : true,\n" +
                "      \"classType\" : \"java.lang.Object\",\n" +
                "      \"@type\" : \"saref:Temperature\",\n" +
                "      \"om:unit_of_measure\" : \"om:degree_Celsius\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"@type\" : \"Thing\",\n" +
                "  \"@context\" : [ \"http://www.w3.org/ns/td\", {\n" +
                "    \"sane\" : \"https://sane.city/\",\n" +
                "    \"saref\" : \"https://w3id.org/saref#\",\n" +
                "    \"sch\" : \"http://schema.org/\",\n" +
                "    \"om\" : \"http://www.wurvoc.org/vocabularies/om-1.8/\"\n" +
                "  } ]\n" +
                "}";

        Thing thing = Thing.fromJson(json);

        assertEquals(1, thing.getPropertiesByObjectType("saref:Temperature").size());
    }

    @Test
    public void getPropertiesByExpandedObjectType() {
        String json = "{\n" +
                "  \"id\" : \"KlimabotschafterWetterstationen:Ahrensburg\",\n" +
                "  \"title\" : \"KlimabotschafterWetterstationen:Ahrensburg\",\n" +
                "  \"properties\" : {\n" +
                "    \"Hum_2m\" : {\n" +
                "      \"description\" : \"Relative Luftfeuchtigkeit 2 m in %\",\n" +
                "      \"type\" : \"number\",\n" +
                "      \"observable\" : true,\n" +
                "      \"readOnly\" : true,\n" +
                "      \"classType\" : \"java.lang.Object\",\n" +
                "      \"@type\" : \"saref:Humidity\"\n" +
                "    },\n" +
                "    \"Temp_2m\" : {\n" +
                "      \"description\" : \"Temperatur in 2m in Grad Celsisus\",\n" +
                "      \"type\" : \"number\",\n" +
                "      \"observable\" : true,\n" +
                "      \"readOnly\" : true,\n" +
                "      \"classType\" : \"java.lang.Object\",\n" +
                "      \"@type\" : \"saref:Temperature\",\n" +
                "      \"om:unit_of_measure\" : \"om:degree_Celsius\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"@type\" : \"Thing\",\n" +
                "  \"@context\" : [ \"http://www.w3.org/ns/td\", {\n" +
                "    \"sane\" : \"https://sane.city/\",\n" +
                "    \"saref\" : \"https://w3id.org/saref#\",\n" +
                "    \"sch\" : \"http://schema.org/\",\n" +
                "    \"om\" : \"http://www.wurvoc.org/vocabularies/om-1.8/\"\n" +
                "  } ]\n" +
                "}";

        Thing thing = Thing.fromJson(json);

        assertEquals(1, thing.getPropertiesByExpandedObjectType("https://w3id.org/saref#Temperature").size());
    }

    @Test
    public void builder() {
        Thing thing = new Thing.Builder()
                .setObjectType("saref:Temperature")
                .setObjectContext(new Context("http://www.w3.org/ns/td"))
                .setId("counter")
                .setTitle("Counter")
                .setDescription("This is a counter")
                .addProperty("count", new ThingProperty.Builder().build())
                .addAction("increment", new ThingAction.Builder().build())
                .addEvent("change", new ThingEvent.Builder().build())
                .addForm(new Form.Builder().build())
                .build();

        assertEquals("saref:Temperature", thing.getObjectType());
        assertEquals(new Context("http://www.w3.org/ns/td"), thing.getObjectContext());
        assertEquals("counter", thing.getId());
        assertEquals("Counter", thing.getTitle());
        assertEquals("This is a counter", thing.getDescription());
        assertEquals(Map.of("count", new ThingProperty.Builder().build()), thing.getProperties());
        assertEquals(Map.of("increment", new ThingAction.Builder().build()), thing.getActions());
        assertEquals(Map.of("change", new ThingEvent.Builder().build()), thing.getEvents());
        assertEquals(new ArrayList<>(Arrays.asList(new Form.Builder().build())), thing.getForms());
    }
}