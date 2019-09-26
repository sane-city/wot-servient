package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.thing.Context;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.property.ExposedThingProperty;
import city.sane.wot.thing.property.ThingProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Produces and exposes luftdaten.info's sensors in given area as things.
 */
public class Luftdaten {
    private final Map<Integer, ExposedThing> things = new HashMap();

    public Luftdaten() throws InterruptedException {
        // create wot
        Wot wot = new DefaultWot();

        while (true) {
            System.out.println("Query luftdaten.info-API");
            try {
                HttpClient client = HttpClientBuilder.create().build();
                HttpGet request = new HttpGet("https://api.luftdaten.info/v1/filter/area=53.599483,9.933534,0.9");
                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();

                ObjectMapper mapper = new ObjectMapper();
                ArrayNode json = (ArrayNode) mapper.readTree(entity.getContent());

                Iterator<JsonNode> measurements = json.elements();
                while (measurements.hasNext()) {
                    JsonNode measurement = measurements.next();
                    JsonNode sensor = measurement.get("sensor");
                    Integer sensorId = sensor.get("id").asInt();
                    ArrayNode sensorDataValues = (ArrayNode) measurement.get("sensordatavalues");
                    JsonNode location = measurement.get("location");

                    Map<String, Double> values = StreamSupport.stream(sensorDataValues.spliterator(), false)
                            .collect(Collectors
                                    .toMap(v -> v.get("value_type").asText(), v -> v.get("value").asDouble()));

                    ExposedThing exposedThing = things.get(sensorId);

                    if (exposedThing == null) {
                        // create and expose thing
                        Thing thing = new Thing.Builder()
                                .setId("luftdaten.info:" + sensorId)
                                .setTitle("luftdaten.info:" + sensorId)
                                .setObjectContexts(new Context("http://www.w3.org/ns/td")
                                        .addContext("om", "http://www.wurvoc.org/vocabularies/om-1.8/")
                                        .addContext("saref", "https://w3id.org/saref#")
                                        .addContext("sch", "http://schema.org/")
                                        .addContext("sane", "https://sane.city/")
                                )
                                .setObjectType("Thing")
                                .build();

                        exposedThing = wot.produce(thing);

                        exposedThing.addProperty(
                                "latitude",
                                new ThingProperty.Builder()
                                        .setObjectType("sch:latitude")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "longitude",
                                new ThingProperty.Builder()
                                        .setObjectType("sch:longitude")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        if (values.containsKey("P1")) {
                            exposedThing.addProperty(
                                    "P1",
                                    new ThingProperty.Builder()
                                            .setDescription("Feinstaub (PM10) in µg/m³")
                                            .setType("number")
                                            .setReadOnly(true)
                                            .setObservable(true)
                                            .build()
                            );
                        }

                        if (values.containsKey("P2")) {
                            exposedThing.addProperty(
                                    "P2",
                                    new ThingProperty.Builder()
                                            .setDescription("Feinstaub (PM2,5) in µg/m³")
                                            .setType("number")
                                            .setReadOnly(true)
                                            .setObservable(true)
                                            .build()
                            );
                        }

                        if (values.containsKey("temperature")) {
                            exposedThing.addProperty(
                                    "temperature",
                                    new ThingProperty.Builder()
                                            .setObjectType("saref:Temperature")
                                            .setDescription("Temperatur in C°")
                                            .setOptional("om:unit_of_measure", "om:degree_Celsius")
                                            .setType("number")
                                            .setReadOnly(true)
                                            .setObservable(true)
                                            .build()
                            );
                        }

                        if (values.containsKey("humidity")) {
                            exposedThing.addProperty(
                                    "humidity",
                                    new ThingProperty.Builder()
                                            .setObjectType("saref:Humidity")
                                            .setDescription("Luftfeuchtigkeit in %")
                                            .setType("number")
                                            .setReadOnly(true)
                                            .setObservable(true)
                                            .build()
                            );
                        }

                        if (values.containsKey("pressure")) {
                            exposedThing.addProperty(
                                    "pressure",
                                    new ThingProperty.Builder()
                                            .setObjectType("saref:Pressure")
                                            .setType("number")
                                            .setReadOnly(true)
                                            .setObservable(true)
                                            .build()
                            );
                        }

                        if (values.containsKey("pressure_at_sealevel")) {
                            exposedThing.addProperty(
                                    "pressure_at_sealevel",
                                    new ThingProperty.Builder()
                                            .setObjectType("saref:Pressure")
                                            .setType("number")
                                            .setReadOnly(true)
                                            .setObservable(true)
                                            .build()
                            );
                        }

                        things.put(sensorId, exposedThing);

                        exposedThing.expose();
                    }

                    // set property values
                    double latitude = location.get("latitude").asDouble();
                    exposedThing.getProperty("latitude").write(latitude);

                    double longitude = location.get("longitude").asDouble();
                    exposedThing.getProperty("longitude").write(longitude);

                    for (Map.Entry<String, Double> entry : values.entrySet()) {
                        String name = entry.getKey();
                        Double value = entry.getValue();

                        ExposedThingProperty property = exposedThing.getProperty(name);
                        if (property != null) {
                            property.write(value);
                        }
                        else {
                            System.err.println("Unknown property found: " + name);
                        }
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            Thread.sleep(60 * 1000);
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        new Luftdaten();
    }
}
