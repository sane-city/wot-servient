package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.Context;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.property.ThingProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Produces and exposes every Klimabotschafter's weather stations as a thing.
 */
public class Klimabotschafter {
    private final Map<String, ExposedThing> things = new HashMap();

    public Klimabotschafter() throws InterruptedException, WotException {
        // create wot
        Wot wot = new DefaultWot();

        while (true) {
            System.out.println("Query Klimabotschafter-API");
            try {
                HttpClient client = HttpClientBuilder.create().build();
                HttpGet request = new HttpGet("http://data.klimabotschafter.de/weatherdata/JSON_Hamburgnet.json");
                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();

                ObjectMapper mapper = new ObjectMapper();
                ObjectNode json = (ObjectNode) mapper.readTree(entity.getContent());

                Iterator<JsonNode> stations = json.elements();
                while (stations.hasNext()) {
                    JsonNode station = stations.next();
                    String st_name = station.get("st_name").asText();

                    ExposedThing exposedThing = things.get(st_name);

                    if (exposedThing == null) {
                        // create and expose thing
                        Thing thing = new Thing.Builder()
                                .setId("KlimabotschafterWetterstationen:" + st_name)
                                .setTitle("KlimabotschafterWetterstationen:" + st_name)
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
                                "Temp_2m",
                                new ThingProperty.Builder()
                                        .setObjectType("saref:Temperature")
                                        .setDescription("Temperatur in 2m in Grad Celsisus")
                                        .setOptional("om:unit_of_measure", "om:degree_Celsius")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "Upload_time",
                                new ThingProperty.Builder()
                                        .setDescription("Letzter Upload der Daten in UTC")
                                        .setType("string")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "Press_sea",
                                new ThingProperty.Builder()
                                        .setObjectType("saref:Pressure")
                                        .setDescription("Luftdruck in hPa auf Meeresnivea")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "Wind_gust",
                                new ThingProperty.Builder()
                                        .setDescription("Stärkste Windböe in m/s der letzten 10 Min")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "Wind_dir",
                                new ThingProperty.Builder()
                                        .setDescription("Windrichtung in Grad (0-360)")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "Rain_year",
                                new ThingProperty.Builder()
                                        .setDescription("Jahresniederschlag")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "Rain_month",
                                new ThingProperty.Builder()
                                        .setDescription("Monatlicher Niederschlag in mm")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "Rain_day",
                                new ThingProperty.Builder()
                                        .setDescription("Tagesniederschlagsmenge (00-00 Uhr) in mm")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "Rain_rate",
                                new ThingProperty.Builder()
                                        .setDescription("Aktuelle Regenrate in mm/hr")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "longitude",
                                new ThingProperty.Builder()
                                        .setObjectType("sch:longitude")
                                        .setDescription("Longitude der Station")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "Wind_avg",
                                new ThingProperty.Builder()
                                        .setDescription("Windgeschwindigkeit (10 Min Mittel) in m/s")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "UV_rad",
                                new ThingProperty.Builder()
                                        .setDescription("UV-Index (Einheit siehe Wikipedia)")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "Hum_2m",
                                new ThingProperty.Builder()
                                        .setObjectType("saref:Humidity")
                                        .setDescription("Relative Luftfeuchtigkeit 2 m in %")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "Solar_rad",
                                new ThingProperty.Builder()
                                        .setDescription("Globalstrahlung in W/m^2")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        exposedThing.addProperty(
                                "latitude",
                                new ThingProperty.Builder()
                                        .setObjectType("sch:latitude")
                                        .setDescription("Latitude")
                                        .setType("number")
                                        .setReadOnly(true)
                                        .setObservable(true)
                                        .build()
                        );

                        things.put(st_name, exposedThing);

                        exposedThing.expose();
                    }

                    // set property values
                    double temp2m = station.get("Temp_2m").asDouble();
                    exposedThing.getProperty("Temp_2m").write(temp2m);

                    String uploadTime = station.get("Upload_time").asText();
                    exposedThing.getProperty("Upload_time").write(uploadTime);

                    double pressSea = station.get("Press_sea").asDouble();
                    exposedThing.getProperty("Press_sea").write(pressSea);

                    double windGust = station.get("Wind_gust").asDouble();
                    exposedThing.getProperty("Wind_gust").write(windGust);

                    int windDir = station.get("Wind_dir").asInt();
                    exposedThing.getProperty("Wind_dir").write(windDir);

                    double rainYear = station.get("Rain_year").asDouble();
                    exposedThing.getProperty("Rain_year").write(rainYear);

                    double rainMonth = station.get("Rain_month").asDouble();
                    exposedThing.getProperty("Rain_month").write(rainMonth);

                    double rainDay = station.get("Rain_day").asDouble();
                    exposedThing.getProperty("Rain_day").write(rainDay);

                    double rainRate = station.get("Rain_rate").asDouble();
                    exposedThing.getProperty("Rain_rate").write(rainRate);

                    double longitude = station.get("longitude").asDouble();
                    exposedThing.getProperty("longitude").write(longitude);

                    double windAvg = station.get("Wind_avg").asDouble();
                    exposedThing.getProperty("Wind_avg").write(windAvg);

                    double uvRad = station.get("UV_rad").asDouble();
                    exposedThing.getProperty("UV_rad").write(uvRad);

                    int hum2m = station.get("Hum_2m").asInt();
                    exposedThing.getProperty("Hum_2m").write(hum2m);

                    double solarRad = station.get("Solar_rad").asDouble();
                    exposedThing.getProperty("Solar_rad").write(solarRad);

                    double latitude = station.get("latitude").asDouble();
                    exposedThing.getProperty("latitude").write(latitude);
                }
            }
            catch (IOException e) {
                // ignore
            }

            Thread.sleep(60 * 1000L);
        }
    }

    public static void main(String[] args) throws InterruptedException, WotException {
        new Klimabotschafter();
    }
}
