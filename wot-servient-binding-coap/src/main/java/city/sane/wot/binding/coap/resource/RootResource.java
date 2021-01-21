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
package city.sane.wot.binding.coap.resource;

import city.sane.wot.binding.coap.WotCoapServer;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint for listing all Things from the {@link city.sane.wot.Servient}.
 */
public class RootResource extends AbstractResource {
    private static final Logger log = LoggerFactory.getLogger(RootResource.class);
    private final WotCoapServer server;

    public RootResource(WotCoapServer server) {
        super("");
        this.server = server;
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        log.debug("Handle GET to '{}'", getURI());

        String requestContentFormat = getOrDefaultRequestContentType(exchange);

        if (ensureSupportedContentFormat(exchange, requestContentFormat)) {
            try {
                Content content = ContentManager.valueToContent(server.getProtocolServer().getThings(), requestContentFormat);
                int contentFormat = MediaTypeRegistry.parse(content.getType());

                exchange.respond(CoAP.ResponseCode.CONTENT, content.getBody(), contentFormat);
            }
            catch (ContentCodecException e) {
                log.warn("Exception", e);
                exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, e.getMessage());
            }
        }
    }
}
