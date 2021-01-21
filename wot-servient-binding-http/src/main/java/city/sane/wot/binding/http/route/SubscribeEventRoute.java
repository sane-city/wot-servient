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

import city.sane.wot.Servient;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.event.ExposedThingEvent;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.Map;

/**
 * Endpoint for interaction with a {@link city.sane.wot.thing.event.ThingEvent}.
 */
public class SubscribeEventRoute extends AbstractInteractionRoute {
    private static final Logger log = LoggerFactory.getLogger(SubscribeEventRoute.class);

    public SubscribeEventRoute(Servient servient, String securityScheme,
                               Map<String, ExposedThing> things) {
        super(servient, securityScheme, things);
    }

    @Override
    protected Object handleInteraction(Request request,
                                       Response response,
                                       String requestContentType,
                                       String name,
                                       ExposedThing thing) {
        ExposedThingEvent<Object> event = thing.getEvent(name);
        if (event != null) {
            Content content = event.observer()
                    .map(optional -> ContentManager.valueToContent(optional.orElse(null), requestContentType))
                    .firstElement().blockingGet();

            if (content != null) {
                log.warn("Next data received for Event '{}': {}", name, content);
                response.type(content.getType());
                return content;
            }
            else {
                response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
                return "";
            }
        }
        else {
            response.status(HttpStatus.NOT_FOUND_404);
            return "Event not found";
        }
    }
}
