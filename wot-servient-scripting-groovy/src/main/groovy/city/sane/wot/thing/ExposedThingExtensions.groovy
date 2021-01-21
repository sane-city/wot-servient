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
package city.sane.wot.thing

import city.sane.wot.thing.ExposedThing
import city.sane.wot.thing.action.ThingAction
import city.sane.wot.thing.event.ThingEvent
import city.sane.wot.thing.property.ThingProperty
import com.fasterxml.jackson.databind.ObjectMapper

import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction

class ExposedThingExtensions {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()

    static ExposedThing addProperty(ExposedThing self, String name, Map map) {
        def property = JSON_MAPPER.convertValue(map, ThingProperty.class)
        return self.addProperty(name, property)
    }

    static ExposedThing addProperty(ExposedThing self, String name, Map map, Object init) {
        def property = JSON_MAPPER.convertValue(map, ThingProperty.class)
        return self.addProperty(name, property, init)
    }

    static ExposedThing addAction(ExposedThing self, String name, Map map, BiFunction<Object, Map<String, Object>, CompletableFuture<Object>> handler) {
        def action = JSON_MAPPER.convertValue(map, ThingAction.class)
        return self.addAction(name, action, handler)
    }

    static ExposedThing addEvent(ExposedThing self, String name, Map map) {
        def event = JSON_MAPPER.convertValue(map, ThingEvent.class)
        return self.addEvent(name, event)
    }
}
