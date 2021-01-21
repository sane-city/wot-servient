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
import city.sane.wot.thing.property.ExposedThingProperty;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint for interaction with a {@link city.sane.wot.thing.property.ThingProperty}.
 */
public class PropertyResource extends AbstractResource {
    private static final Logger log = LoggerFactory.getLogger(PropertyResource.class);
    private final ExposedThingProperty<Object> property;

    public PropertyResource(String name, ExposedThingProperty<Object> property) {
        super(name);
        this.property = property;
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        log.debug("Handle GET to '{}'", getURI());
        if (!property.isWriteOnly()) {
            String requestContentFormat = getOrDefaultRequestContentType(exchange);

            property.read().whenComplete((value, e) -> {
                if (e == null) {
                    try {
                        Content content = ContentManager.valueToContent(value, requestContentFormat);

                        int contentFormat = MediaTypeRegistry.parse(content.getType());
                        exchange.respond(CoAP.ResponseCode.CONTENT, content.getBody(), contentFormat);
                    }
                    catch (ContentCodecException ex) {
                        log.warn("Unable to serialize new property value", ex);
                        exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, ex.toString());
                    }
                }
                else {
                    log.warn("Unable to read property value", e);
                    exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, e.toString());
                }
            });
        }
        else {
            exchange.respond(CoAP.ResponseCode.METHOD_NOT_ALLOWED, "Property writeOnly");
        }
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        log.debug("WotCoapServer handles PUT to {}", getURI());

        String requestContentFormat = getOrDefaultRequestContentType(exchange);
        if (!ContentManager.isSupportedMediaType(requestContentFormat)) {
            log.warn("Unsupported media type: {}", requestContentFormat);
            String payload = "Unsupported Media Type (supported: " + String.join(", ", ContentManager.getSupportedMediaTypes()) + ")";
            exchange.respond(CoAP.ResponseCode.UNSUPPORTED_CONTENT_FORMAT, payload);
        }

        if (!property.isReadOnly()) {
            writeProperty(exchange, requestContentFormat);
        }
        else {
            exchange.respond(CoAP.ResponseCode.METHOD_NOT_ALLOWED, "Property writeOnly");
        }
    }

    private void writeProperty(CoapExchange exchange, String requestContentFormat) {
        byte[] requestPayload = exchange.getRequestPayload();

        try {
            Object input = ContentManager.contentToValue(new Content(requestContentFormat, requestPayload), property);

            property.write(input).whenComplete((output, e) -> {
                if (e == null) {
                    if (output != null) {
                        try {
                            Content outputContent = ContentManager.valueToContent(output, requestContentFormat);
                            int outputContentFormat = MediaTypeRegistry.parse(outputContent.getType());
                            exchange.respond(CoAP.ResponseCode.CHANGED, outputContent.getBody(), outputContentFormat);
                        }
                        catch (ContentCodecException ex) {
                            exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, ex.getMessage());
                        }
                    }
                    else {
                        exchange.respond(CoAP.ResponseCode.CHANGED, new byte[0], MediaTypeRegistry.parse(requestContentFormat));
                    }
                }
                else {
                    log.warn("Unable to write property value", e);
                    exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, e.getMessage());
                }
            });
        }
        catch (ContentCodecException ex) {
            exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, ex.getMessage());
        }
    }
}
