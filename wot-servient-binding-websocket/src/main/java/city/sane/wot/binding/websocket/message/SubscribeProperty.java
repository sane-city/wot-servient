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
package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.property.ExposedThingProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

public class SubscribeProperty extends ThingInteraction {
    private final Logger log = LoggerFactory.getLogger(SubscribeProperty.class);

    private SubscribeProperty() {
        super();
    }

    public SubscribeProperty(String thingId, String name) {
        super(thingId, name);
    }

    @Override
    public void reply(Consumer<AbstractServerMessage> replyConsumer,
                      Map<String, ExposedThing> things) {
        String id = getThingId();
        ExposedThing thing = things.get(id);

        if (thing != null) {
            String name = getName();
            ExposedThingProperty<Object> property = thing.getProperty(name);

            if (property != null) {
                property.observer()
                        .map(optional -> ContentManager.valueToContent(optional.orElse(null)))
                        .subscribe(
                                content -> {
                                    log.debug("Next data received for Property '{}'", name);
                                    replyConsumer.accept(new SubscribeNextResponse(getId(), content));
                                },
                                e -> {
                                    log.warn("Cannot process data for Property '{}':", name, e);
                                    replyConsumer.accept(new ServerErrorResponse(this, "Subscription produced error: " + e.getMessage()));
                                },
                                () -> replyConsumer.accept(new SubscribeCompleteResponse(getId())));
            }
            else {
                // Property not found
                replyConsumer.accept(new ClientErrorResponse(this, "Property not found"));
            }
        }
        else {
            // Thing not found
            replyConsumer.accept(new ClientErrorResponse(this, "Thing not found"));
        }
    }

    @Override
    public String toString() {
        return "SubscribeProperty{" +
                "thingId='" + thingId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
