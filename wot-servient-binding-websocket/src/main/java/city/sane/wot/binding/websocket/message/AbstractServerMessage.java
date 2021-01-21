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

import city.sane.wot.content.Content;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract class for all messages sent from {@link city.sane.wot.binding.websocket.WebsocketProtocolServer}
 * to {@link city.sane.wot.binding.websocket.WebsocketProtocolClient}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReadPropertyResponse.class, name = "ReadPropertyResponse"),
        @JsonSubTypes.Type(value = WritePropertyResponse.class, name = "WritePropertyResponse"),
        @JsonSubTypes.Type(value = InvokeActionResponse.class, name = "InvokeActionResponse"),
        @JsonSubTypes.Type(value = SubscribeNextResponse.class, name = "SubscribeNextResponse"),
        @JsonSubTypes.Type(value = SubscribeErrorResponse.class, name = "SubscribeErrorResponse"),
        @JsonSubTypes.Type(value = SubscribeCompleteResponse.class, name = "SubscribeCompleteResponse"),
        @JsonSubTypes.Type(value = ServerErrorResponse.class, name = "ServerErrorResponse"),
        @JsonSubTypes.Type(value = ClientErrorResponse.class, name = "ClientErrorResponse")
})
public abstract class AbstractServerMessage {
    protected final String id;

    public AbstractServerMessage(AbstractClientMessage message) {
        this(message.getId());
    }

    public AbstractServerMessage(String id) {
        this.id = id;
    }

    protected AbstractServerMessage() {
        id = null;
    }

    public String getId() {
        return id;
    }

    public abstract Content toContent();
}
