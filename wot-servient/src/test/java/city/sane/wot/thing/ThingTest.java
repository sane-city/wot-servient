package city.sane.wot.thing;

import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.property.ThingProperty;
import com.github.jsonldjava.shaded.com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.*;

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
    public void toJsonPretty() {
        Thing thing = new Thing.Builder()
                .setId("Foo")
                .setDescription("Bar")
                .setObjectType("Thing")
                .setObjectContext(new Context("http://www.w3.org/ns/td"))
                .build();

        assertEquals(
                "{\n" +
                        "  \"id\" : \"Foo\",\n" +
                        "  \"description\" : \"Bar\",\n" +
                        "  \"@type\" : \"Thing\",\n" +
                        "  \"@context\" : \"http://www.w3.org/ns/td\"\n" +
                        "}",
                thing.toJson(true)
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
    public void fromJsonFile() throws IOException {
        String json = "{\"id\":\"Foo\",\"description\":\"Bar\",\"@type\":\"Thing\",\"@context\":[\"http://www.w3.org/ns/td\"]}";

        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        File file = folder.newFile("counter.json");
        Files.write(json, file, Charset.defaultCharset());

        Thing thing = Thing.fromJson(file);

        assertEquals("Foo", thing.getId());
        assertEquals("Bar", thing.getDescription());
        assertEquals("Thing", thing.getObjectType());
        assertEquals(new Context("http://www.w3.org/ns/td"), thing.getObjectContext());
    }

    @Test
    public void fromMap() {
        Map map = Map.of(
                "id", "Foo",
                "description", "Bar",
                "@type", "Thing",
                "@context", Arrays.asList("http://www.w3.org/ns/td")
        );
        Thing thing = Thing.fromMap(map);

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
                .setTitles(Map.of("de", "Zähler"))
                .setDescription("This is a counter")
                .setDescriptions(Map.of("de", "Zähler Ding"))
                .setProperties(new HashMap<>(Map.of("lastChange", new ThingProperty.Builder().build())))
                .addProperty("count", new ThingProperty.Builder().build())
                .setActions(new HashMap<>(Map.of("decrement", new ThingAction.Builder().build())))
                .addAction("increment", new ThingAction.Builder().build())
                .setEvents(new HashMap<>(Map.of("ping", new ThingEvent.Builder().build())))
                .addEvent("change", new ThingEvent.Builder().build())
                .setForms(Lists.newArrayList(new Form.Builder().setHref("http://eins").build()))
                .addForm(new Form.Builder().setHref("http://zwei").build())
                .setBase("http://sane.city")
                .build();

        assertEquals("saref:Temperature", thing.getObjectType());
        assertEquals(new Context("http://www.w3.org/ns/td"), thing.getObjectContext());
        assertEquals("counter", thing.getId());
        assertEquals("Counter", thing.getTitle());
        assertEquals("This is a counter", thing.getDescription());
        assertEquals("http://sane.city", thing.getBase());

        assertThat((Map<String, String>) thing.getTitles(), hasEntry("de", "Zähler"));
        assertThat((Map<String, String>) thing.getDescriptions(), hasEntry("de", "Zähler Ding"));

        assertThat((Map<String, ThingProperty>) thing.getProperties(), hasEntry("lastChange", new ThingProperty.Builder().build()));
        assertThat((Map<String, ThingProperty>) thing.getProperties(), hasEntry("count", new ThingProperty.Builder().build()));
        assertEquals(new ThingProperty.Builder().build(), thing.getProperty("count"));

        assertThat((Map<String, ThingAction>) thing.getActions(), hasEntry("increment", new ThingAction.Builder().build()));
        assertThat((Map<String, ThingAction>) thing.getActions(), hasEntry("decrement", new ThingAction.Builder().build()));
        assertEquals(new ThingAction.Builder().build(), thing.getAction("increment"));

        assertThat((Map<String, ThingEvent>) thing.getEvents(), hasEntry("change", new ThingEvent.Builder().build()));
        assertThat((Map<String, ThingEvent>) thing.getEvents(), hasEntry("ping", new ThingEvent.Builder().build()));
        assertEquals(new ThingEvent.Builder().build(), thing.getEvent("ping"));

        assertThat((Collection<Form>) thing.getForms(), contains(new Form.Builder().setHref("http://eins").build(), new Form.Builder().setHref("http://zwei").build()));
    }

    @Test
    public void testEquals() {
        Thing thingA = new Thing.Builder().setId("counter").build();
        Thing thingB = new Thing.Builder().setId("counter").build();

        assertEquals(thingA, thingB);
    }

    @Test
    public void getExpandedObjectType() {
        Thing thing = new Thing.Builder()
                .setId("Foo")
                .setDescription("Bar")
                .setObjectType("Thing")
                .setObjectContext(new Context("http://www.w3.org/ns/td").addContext("saref", "https://w3id.org/saref#"))
                .build();

        assertEquals("https://w3id.org/saref#Temperature", thing.getExpandedObjectType("saref:Temperature"));
    }

    @Test
    public void randomId() {
        assertNotEquals(Thing.randomId(), Thing.randomId());
    }
}