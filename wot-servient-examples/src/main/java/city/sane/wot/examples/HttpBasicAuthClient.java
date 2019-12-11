package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.ConsumedThing;

import java.util.concurrent.ExecutionException;

/**
 * Interacts with a Thing that is secured with HTTP Basic Auth.
 *
 * application.conf:
 *
 * wot {
 *   servient {
 *     credentials {
 *       "urn:dev:wot:http:auth:basic" = {
 *         username = "guest"
 *         password = "guest"
 *       }
 *     }
 *   }
 * }
 */
class HttpBasicAuthClient {
    public static void main(String[] args) throws ExecutionException, InterruptedException, WotException {
        Wot wot = DefaultWot.clientOnly();

        String thing = "{\n" +
                "    \"@context\": \"https://www.w3.org/2019/td/v1\",\n" +
                "    \"title\": \"HTTP Basic Auth\",\n" +
                "    \"id\": \"urn:dev:wot:http:auth:basic\",\n" +
                "    \"securityDefinitions\": {\n" +
                "        \"basic_sc\": {\n" +
                "            \"scheme\": \"basic\",\n" +
                "            \"in\": \"header\"\n" +
                "        }\n" +
                "    }," +
                "    \"security\": [\"basic_sc\"],\n" +
                "    \"actions\" : {\n" +
                "        \"fire\": {\n" +
                "            \"forms\": [\n" +
                "                {\"href\": \"https://jigsaw.w3.org/HTTP/Basic/\"}\n" +
                "            ]\n" +
                "        }\n" +
                "    } \n" +
                "}";

        ConsumedThing consumedThing = wot.consume(thing);

        Object output = consumedThing.getAction("fire").invoke().get();
        System.out.println(output);
    }
}
