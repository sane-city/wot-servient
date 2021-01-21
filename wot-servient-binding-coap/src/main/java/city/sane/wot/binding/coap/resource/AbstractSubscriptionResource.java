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

import city.sane.Pair;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import io.reactivex.rxjava3.core.Observable;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.observe.ObserveRelation;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

abstract class AbstractSubscriptionResource extends AbstractResource {
    private static final Logger log = LoggerFactory.getLogger(AbstractSubscriptionResource.class);
    private final String name;
    private final Observable<Optional<Object>> observable;
    private Pair<Optional, Throwable> last;

    AbstractSubscriptionResource(String resourceName,
                                 String name,
                                 Observable<Optional<Object>> observable) {
        super(resourceName);

        this.name = name;
        this.observable = observable;

        setObservable(true); // enable observing
        setObserveType(CoAP.Type.CON); // configure the notification type to CONs
        getAttributes().setObservable(); // mark observable in the Link-Format

        observable.subscribe(
                optional -> {
                    last = new Pair<>(optional, null);
                    changed();
                },
                e -> {
                    last = new Pair<>(null, e);
                    changed();
                }
        );
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        log.debug("Handle GET to '{}'", getURI());

        if (!exchange.advanced().getRequest().isAcknowledged()) {
            // The requestor should only be informed about new values.
            // send acknowledgement
            exchange.accept();

            ObserveRelation relation = exchange.advanced().getRelation();
            relation.setEstablished(true);
            addObserveRelation(relation);
        }
        else {
            Optional optional = last.first();
            Throwable e = last.second();

            String requestContentFormat = getOrDefaultRequestContentType(exchange);
            String subscribableType = observable.getClass().getSimpleName();
            if (e == null) {
                try {
                    Object data = optional.orElse(null);
                    log.debug("New data received for {} '{}': {}", subscribableType, name, data);
                    Content content = ContentManager.valueToContent(data, requestContentFormat);

                    int contentFormat = MediaTypeRegistry.parse(content.getType());
                    byte[] body = content.getBody();
                    if (body.length > 0) {
                        exchange.respond(CoAP.ResponseCode.CONTENT, body, contentFormat);
                    }
                    else {
                        exchange.respond(CoAP.ResponseCode.CONTENT);
                    }
                }
                catch (ContentCodecException ex) {
                    log.warn("Cannot process data for {} '{}': {}", subscribableType, name, ex.getMessage());
                    exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, "Invalid " + subscribableType + " Data");
                }
            }
            else {
                log.warn("Cannot process data for {} '{}': {}", subscribableType, name, e.getMessage());
                exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, "Invalid " + subscribableType + " Data");
            }
        }
    }
}
