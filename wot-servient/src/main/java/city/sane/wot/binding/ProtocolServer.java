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
package city.sane.wot.binding;

import city.sane.wot.Servient;
import city.sane.wot.thing.ExposedThing;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * A ProtocolServer defines how to expose Thing for interaction via a specific protocol (e.g. HTTP,
 * MQTT, etc.).
 */
public interface ProtocolServer {
    /**
     * Starts the server (e.g. HTTP server) and makes it ready for requests to the exposed things.
     *
     * @param servient
     * @return
     */
    CompletableFuture<Void> start(Servient servient);

    /**
     * Stops the server (e.g. HTTP server) and ends the exposure of the Things
     *
     * @return
     */
    CompletableFuture<Void> stop();

    /**
     * Exposes <code>thing</code> and allows interaction with it.
     *
     * @param thing
     * @return
     */
    CompletableFuture<Void> expose(ExposedThing thing);

    /**
     * Stops the exposure of <code>thing</code> and allows no further interaction with the thing.
     *
     * @param thing
     * @return
     */
    CompletableFuture<Void> destroy(ExposedThing thing);

    /**
     * Returns the URL to the Thing Directory if the server supports the listing of all Thing
     * Descriptions.
     *
     * @return
     * @throws ProtocolServerException
     */
    default URI getDirectoryUrl() throws ProtocolServerException {
        throw new ProtocolServerNotImplementedException(getClass(), "directory");
    }

    /**
     * Returns the URL to the thing with the id <code>id</code> if the server supports the listing
     * of certain Thing Descriptions.
     *
     * @param id
     * @return
     * @throws ProtocolServerException
     */
    default URI getThingUrl(String id) throws ProtocolServerException {
        throw new ProtocolServerNotImplementedException(getClass(), "thing-url");
    }
}
