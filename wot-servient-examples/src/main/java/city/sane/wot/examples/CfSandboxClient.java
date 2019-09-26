package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.Thing;

import java.util.concurrent.ExecutionException;

/**
 * Consume thing description from string and then interact with the thing via coap.
 */
public class CfSandboxClient {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // create wot
        Wot wot = DefaultWot.clientOnly();

        Thing thing = Thing.fromJson("{\n" +
                "    \"@context \": \"https://www.w3.org/2019/td/v1\",\n" +
                "    \"id\": \"urn:dev:wot:org:eclipse:cf-sandbox\",\n" +
                "    \"title\": \"Cf-Sandbox\",\n" +
                "    \"description\": \"Californium online example server (coap://californium.eclipse.org/)\",\n" +
                "    \"securityDefinitions\": {\n" +
                "        \"none\": {\"scheme\": \"nosec\"}\n" +
                "    },\n" +
                "    \"security\": \"none\",\n" +
                "    \"properties\": {\n" +
                "        \"test\": {\n" +
                "            \"description\": \"Test CoAP resource\",\n" +
                "            \"type\": \"string\",\n" +
                "            \"forms\": [\n" +
                "                {\n" +
                "                    \"href\": \"coap://californium.eclipse.org:5683/test\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    }\n" +
                "}");

        ConsumedThing consumedThing = wot.consume(thing);

        Object value = consumedThing.getProperty("test").read().get();

        System.out.println("CfSandboxClient: Received: " + value);
    }
}
