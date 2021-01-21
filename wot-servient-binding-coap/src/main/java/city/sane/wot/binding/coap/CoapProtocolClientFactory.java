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
package city.sane.wot.binding.coap;

import city.sane.wot.binding.ProtocolClientFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Creates new {@link CoapProtocolClient} instances.
 */
public class CoapProtocolClientFactory implements ProtocolClientFactory {
    static {
        // Californium uses java.util.logging. We need to redirect all log messages to logback
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private final ExecutorService executor;

    public CoapProtocolClientFactory() {
        executor = Executors.newFixedThreadPool(10);
    }

    @Override
    public String toString() {
        return "CoapClient";
    }

    @Override
    public String getScheme() {
        return "coap";
    }

    @Override
    public CoapProtocolClient getClient() {
        return new CoapProtocolClient(executor);
    }
}
