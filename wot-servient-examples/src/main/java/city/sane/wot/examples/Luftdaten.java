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
package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Produces and exposes luftdaten.info's sensors in given area as things.
 */
@SuppressWarnings({ "squid:S2925", "java:S106", "java:S1192" })
class Luftdaten {
    private Luftdaten() throws InterruptedException, WotException {
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

                    Map<Integer, ExposedThing> things = new HashMap();
                    ExposedThing exposedThing = things.get(sensorId);

                    if (exposedThing == null) {
                        // create and expose thing
                        Thing thing = new Thing.Builder()
                                .setId("luftdaten.info:" + sensorId)
                                .setTitle("luftdaten.info:" + sensorId)
                                .setObjectContext(new Context("http://www.w3.org/ns/td")
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

                        ExposedThingProperty<Object> property = exposedThing.getProperty(name);
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
                // ignore
            }

            Thread.sleep(60 * 1000L);
        }
    }

    public static void main(String[] args) throws InterruptedException, WotException {
        new Luftdaten();
    }
}
