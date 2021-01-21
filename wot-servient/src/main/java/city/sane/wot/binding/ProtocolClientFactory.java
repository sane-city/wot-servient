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

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * A ProtocolClientFactory is responsible for creating new {@link ProtocolClient} instances. There
 * is a separate client instance for each {@link city.sane.wot.thing.ConsumedThing}.
 */
public interface ProtocolClientFactory {
    String getScheme();

    ProtocolClient getClient() throws ProtocolClientException;

    /**
     * Is called on servient start.
     *
     * @return
     */
    default CompletableFuture<Void> init() {
        return completedFuture(null);
    }

    /**
     * Is called on servient shutdown.
     *
     * @return
     */
    default CompletableFuture<Void> destroy() {
        return completedFuture(null);
    }
}
