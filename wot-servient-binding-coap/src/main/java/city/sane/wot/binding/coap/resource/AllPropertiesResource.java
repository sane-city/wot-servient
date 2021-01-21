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

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint for reading all properties from a Thing
 */
public class AllPropertiesResource extends AbstractResource {
    private static final Logger log = LoggerFactory.getLogger(AllPropertiesResource.class);
    private final ExposedThing thing;

    public AllPropertiesResource(ExposedThing thing) {
        super("properties");
        this.thing = thing;
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        log.debug("Handle GET to '{}'", getURI());

        String requestContentFormat = getOrDefaultRequestContentType(exchange);

        if (ensureSupportedContentFormat(exchange, requestContentFormat)) {
            thing.readProperties().whenComplete((values, e) -> {
                if (e == null) {
                    // remove writeOnly properties
                    values.entrySet().removeIf(entry -> thing.getProperty(entry.getKey()).isWriteOnly());

                    try {
                        Content content = ContentManager.valueToContent(values, requestContentFormat);

                        int contentFormat = MediaTypeRegistry.parse(content.getType());
                        exchange.respond(CoAP.ResponseCode.CONTENT, content.getBody(), contentFormat);
                    }
                    catch (ContentCodecException ex) {
                        log.warn("Exception", ex);
                        exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, ex.toString());
                    }
                }
                else {
                    log.warn("Exception", e);
                    exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, e.toString());
                }
            });
        }
    }
}
