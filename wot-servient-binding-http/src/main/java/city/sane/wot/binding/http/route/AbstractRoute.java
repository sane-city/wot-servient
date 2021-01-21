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
package city.sane.wot.binding.http.route;

import city.sane.wot.content.ContentManager;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import static city.sane.wot.util.LoggingUtil.sanitizeLogArg;

/**
 * Abstract route for exposing Things. Inherited from all other routes.
 */
abstract class AbstractRoute implements Route {
    static final Logger log = LoggerFactory.getLogger(AbstractRoute.class);

    String getOrDefaultRequestContentType(Request request) {
        if (request.contentType() != null) {
            return request.contentType();
        }
        else {
            return ContentManager.DEFAULT;
        }
    }

    String unsupportedMediaTypeResponse(Response response, String requestContentType) {
        if (!ContentManager.isSupportedMediaType(requestContentType)) {
            log.warn("Unsupported media type: {}", sanitizeLogArg(requestContentType));
            response.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415);
            return "Unsupported Media Type (supported: " + String.join(", ", ContentManager.getSupportedMediaTypes()) + ")";
        }
        else {
            return null;
        }
    }

    void logRequest(Request request) {
        if (log.isDebugEnabled()) {
            log.debug("Handle {} to '{}'", request.requestMethod(), request.url());
            if (request.raw().getQueryString() != null && !request.raw().getQueryString().isEmpty()) {
                log.debug("Request parameters: {}", request.raw().getQueryString());
            }
        }
    }
}
