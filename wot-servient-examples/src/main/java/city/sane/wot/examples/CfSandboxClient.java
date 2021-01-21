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
import city.sane.wot.thing.ConsumedThing;

import java.util.concurrent.ExecutionException;

/**
 * Consume thing description from string and then interact with the thing via coap.
 */
@SuppressWarnings({ "java:S106" })
class CfSandboxClient {
    public static void main(String[] args) throws ExecutionException, InterruptedException, WotException {
        // create wot
        Wot wot = DefaultWot.clientOnly();

        String thing = "{\n" +
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
                "}";

        ConsumedThing consumedThing = wot.consume(thing);

        Object value = consumedThing.getProperty("test").read().get();

        System.out.println("CfSandboxClient: Received: " + value);
    }
}
