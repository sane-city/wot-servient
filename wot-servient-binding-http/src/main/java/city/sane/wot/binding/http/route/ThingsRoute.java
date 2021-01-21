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

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import spark.Request;
import spark.Response;

import java.util.Map;

/**
 * Endpoint for listing all Things from the {@link city.sane.wot.Servient}.
 */
public class ThingsRoute extends AbstractRoute {
    private final Map<String, ExposedThing> things;

    public ThingsRoute(Map<String, ExposedThing> things) {
        this.things = things;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        logRequest(request);

        String requestContentType = getOrDefaultRequestContentType(request);

        String unsupportedMediaTypeResponse = unsupportedMediaTypeResponse(response, requestContentType);
        if (unsupportedMediaTypeResponse != null) {
            return unsupportedMediaTypeResponse;
        }

        Content content = ContentManager.valueToContent(things, requestContentType);
        response.type(requestContentType);
        return content;
    }
}
