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

import city.sane.wot.content.ContentManager;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract resource for exposing Things. Inherited from all other resources.
 */
abstract class AbstractResource extends CoapResource {
    private static final Logger log = LoggerFactory.getLogger(AbstractResource.class);

    AbstractResource(String name) {
        super(name);
    }

    String getOrDefaultRequestContentType(CoapExchange exchange) {
        int requestContentFormatNum = exchange.getRequestOptions().getContentFormat();
        if (requestContentFormatNum != -1) {
            return MediaTypeRegistry.toString(requestContentFormatNum);
        }
        else {
            return ContentManager.DEFAULT;
        }
    }

    boolean ensureSupportedContentFormat(CoapExchange exchange, String requestContentFormat) {
        if (ContentManager.isSupportedMediaType(requestContentFormat)) {
            return true;
        }
        else {
            log.warn("Unsupported media type: {}", requestContentFormat);
            String payload = "Unsupported Media Type (supported: " + String.join(", ", ContentManager.getSupportedMediaTypes()) + ")";
            exchange.respond(CoAP.ResponseCode.UNSUPPORTED_CONTENT_FORMAT, payload);
            return false;
        }
    }
}
