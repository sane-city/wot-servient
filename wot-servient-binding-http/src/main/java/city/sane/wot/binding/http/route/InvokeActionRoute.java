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
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Endpoint for invoking a {@link city.sane.wot.thing.action.ThingAction}.
 */
public class InvokeActionRoute extends AbstractInteractionRoute {
    static final Logger log = LoggerFactory.getLogger(InvokeActionRoute.class);

    public InvokeActionRoute(Servient servient, String securityScheme,
                             Map<String, ExposedThing> things) {
        super(servient, securityScheme, things);
    }

    @Override
    protected Object handleInteraction(Request request,
                                       Response response,
                                       String requestContentType,
                                       String name,
                                       ExposedThing thing) {
        ExposedThingAction<Object, Object> action = thing.getAction(name);
        if (action != null) {
            try {
                Content content = new Content(requestContentType, request.bodyAsBytes());
                Object input = ContentManager.contentToValue(content, action.getInput());

                Map<String, Map<String, Object>> options = Map.of(
                        "uriVariables", parseUrlParameters(request.queryMap().toMap(), action.getUriVariables())
                );

                Object value = action.invoke(input, options).get();

                return respondWithValue(response, requestContentType, content, value);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
            catch (ContentCodecException | ExecutionException e) {
                response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
                return e;
            }
        }
        else {
            response.status(HttpStatus.NOT_FOUND_404);
            return "Action not found";
        }
    }

    private Map<String, Object> parseUrlParameters(Map<String, String[]> urlParams,
                                                   Map<String, Map<String, Object>> uriVariables) {
        log.debug("parse url parameters '{}' with uri variables '{}'", urlParams.keySet(), uriVariables);
        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : urlParams.entrySet()) {
            String name = entry.getKey();
            String[] urlValue = entry.getValue();

            Map uriVariable = uriVariables.get(name);
            if (uriVariable != null) {
                Object type = uriVariable.get("type");

                if (type != null) {
                    if (type.equals("integer") || type.equals("number")) {
                        Integer value = Integer.valueOf(urlValue[0]);
                        params.put(name, value);
                    }
                    else if (type.equals("string")) {
                        String value = urlValue[0];
                        params.put(name, value);
                    }
                    else {
                        log.warn("Not able to read variable '{}' because variable type '{}' is unknown", name, type);
                    }
                }
            }
        }
        return params;
    }

    private Object respondWithValue(Response response,
                                    String requestContentType,
                                    Content content,
                                    Object value) {
        try {
            Content outputContent = ContentManager.valueToContent(value, requestContentType);

            if (value != null) {
                response.type(content.getType());
                return outputContent;
            }
            else {
                return "";
            }
        }
        catch (ContentCodecException e) {
            response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
            return e;
        }
    }
}
