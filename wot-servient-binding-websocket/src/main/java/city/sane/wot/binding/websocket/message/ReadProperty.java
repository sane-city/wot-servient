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

import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.property.ExposedThingProperty;

import java.util.Map;
import java.util.function.Consumer;

public class ReadProperty extends ThingInteraction {
    private ReadProperty() {
        super();
    }

    public ReadProperty(String thingId, String name) {
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
                property.read().thenAccept(value -> {
                    try {
                        replyConsumer.accept(new ReadPropertyResponse(this, ContentManager.valueToContent(value)));
                    }
                    catch (ContentCodecException e) {
                        replyConsumer.accept(new ServerErrorResponse(this, "Unable to parse output of write operation: " + e.getMessage()));
                    }
                });
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
        return "ReadProperty{" +
                "thingId='" + thingId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
