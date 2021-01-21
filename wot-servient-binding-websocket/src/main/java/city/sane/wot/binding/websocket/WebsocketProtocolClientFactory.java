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
package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClientFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Creates new {@link WebsocketProtocolClient} instances.
 */
public class WebsocketProtocolClientFactory implements ProtocolClientFactory {
    private final Set<WebsocketProtocolClient> clients = new HashSet<>();

    @Override
    public String getScheme() {
        return "ws";
    }

    @Override
    public WebsocketProtocolClient getClient() {
        WebsocketProtocolClient client = new WebsocketProtocolClient();
        clients.add(client);
        return client;
    }

    @Override
    public CompletableFuture<Void> destroy() {
        clients.forEach(WebsocketProtocolClient::destroy);
        return completedFuture(null);
    }
}
