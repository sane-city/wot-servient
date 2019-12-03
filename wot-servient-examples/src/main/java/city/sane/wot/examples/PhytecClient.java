package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.thing.ConsumedThing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * This examples read properties from a phyNODE.
 */
public class PhytecClient {
    public PhytecClient() throws URISyntaxException {
        // create wot
        Wot wot = DefaultWot.clientOnly();

        URI url = new URI("file:/home/user/sdw/thingDirectory/SANE_PHYTEC_E50209.json");
        System.out.println("fetch");
        wot.fetch(url).whenComplete((thing, e) -> {
            try {
                if (e != null) {
                    throw e;
                }

                System.out.println("=== TD ===");
                String json = thing.toJson(true);
                System.out.println(json);
                System.out.println("==========");

                ConsumedThing consumedThing = wot.consume(thing);

                while (true) {
                    // read temp
                    Map<String, Object> temp = (Map) consumedThing.getProperty("temp").read().get();
                    Double tempValue = (Double) temp.get("value");
                    String tempUnit = (String) temp.get("unit");
                    System.out.printf("%s: Temperature is '%.2f %s'\n", thing.getId(), tempValue, tempUnit);

                    // read hum
                    Map<String, Object> hum = (Map) consumedThing.getProperty("hum").read().get();
                    Double humValue = (Double) hum.get("value");
                    String humUnit = (String) hum.get("unit");
                    System.out.printf("%s: Humidity is '%.2f %s'\n", thing.getId(), humValue, humUnit);

                    // read press
                    Map<String, Object> press = (Map) consumedThing.getProperty("hum").read().get();
                    Double pressValue = (Double) press.get("value");
                    String pressUnit = (String) press.get("unit");
                    System.out.printf("%s: Pressure is '%.2f %s'\n", thing.getId(), pressValue, pressUnit);

                    // read btn
                    Map<String, Object> btn = (Map) consumedThing.getProperty("btn").read().get();
                    boolean btnValue = ((Double) btn.get("value")) > 0;
                    if (btnValue) {
                        System.out.printf("%s: Button is pressed\n", thing.getId());
                    }
                    else {
                        System.out.printf("%s: Button is not pressed\n", thing.getId());
                    }

                    Thread.sleep(10 * 1000L);
                }

            }
            catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }).join();
    }

    public static void main(String[] args) throws URISyntaxException {
        new PhytecClient();
    }
}
