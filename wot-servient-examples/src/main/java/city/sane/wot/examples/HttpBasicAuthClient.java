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
 * Interacts with a Thing that is secured with HTTP Basic Auth.
 * <p>
 * application.conf:
 * <p>
 * wot { servient { credentials { "urn:dev:wot:http:auth:basic" = { username = "guest" password =
 * "guest" } } } }
 */
@SuppressWarnings({ "java:S106" })
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
                "                {\"href\": \"https://jigsaw.w3.org/HTTP/Basic/\", \"htv:methodName\": \"GET\"}\n" +
                "            ]\n" +
                "        }\n" +
                "    } \n" +
                "}";

        ConsumedThing consumedThing = wot.consume(thing);

        Object output = consumedThing.getAction("fire").invoke().get();
        System.out.println(output);
    }
}
