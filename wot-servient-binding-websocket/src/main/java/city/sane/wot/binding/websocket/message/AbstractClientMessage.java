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
package city.sane.wot.binding.websocket.message;

import city.sane.wot.thing.ExposedThing;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Abstract class for all messages sent from {@link city.sane.wot.binding.websocket.WebsocketProtocolClient}
 * to {@link city.sane.wot.binding.websocket.WebsocketProtocolServer}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReadProperty.class, name = "ReadProperty"),
        @JsonSubTypes.Type(value = WriteProperty.class, name = "WriteProperty"),
        @JsonSubTypes.Type(value = InvokeAction.class, name = "InvokeAction"),
        @JsonSubTypes.Type(value = SubscribeProperty.class, name = "SubscribeProperty"),
        @JsonSubTypes.Type(value = SubscribeEvent.class, name = "SubscribeEvent")
})
public abstract class AbstractClientMessage {
    private final String id;

    protected AbstractClientMessage() {
        id = randomId();
    }

    /**
     * Generates a new random ID to make the message uniquely identifiable.
     *
     * @return
     */
    public static String randomId() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    public String getId() {
        return id;
    }

    /**
     * Creates the server's response to the request sent by the client.
     *
     * @param replyConsumer
     * @param things
     * @return
     */
    public abstract void reply(Consumer<AbstractServerMessage> replyConsumer,
                               Map<String, ExposedThing> things);
}
