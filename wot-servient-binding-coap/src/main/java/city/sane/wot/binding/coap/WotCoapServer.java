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

import city.sane.wot.binding.coap.resource.RootResource;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * We have to define our own root resource because we want to dynamically determine the children
 * resources at runtime.
 */
public class WotCoapServer extends org.eclipse.californium.core.CoapServer {
    private final CoapProtocolServer protocolServer;

    public WotCoapServer(CoapProtocolServer protocolServer) {
        super(protocolServer.getBindPort());
        this.protocolServer = protocolServer;
    }

    @Override
    protected Resource createRoot() {
        return new RootResource(this);
    }

    public CoapProtocolServer getProtocolServer() {
        return protocolServer;
    }

    public int getPort() {
        return getEndpoints().get(0).getAddress().getPort();
    }
}
