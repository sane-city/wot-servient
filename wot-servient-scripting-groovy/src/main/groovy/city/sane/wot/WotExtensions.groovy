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
package city.sane.wot


import city.sane.wot.Wot
import city.sane.wot.thing.ConsumedThing
import city.sane.wot.thing.ExposedThing
import city.sane.wot.thing.Thing
import com.fasterxml.jackson.databind.ObjectMapper

class WotExtensions {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()

    static ExposedThing produce(Wot self, Map map) {
        def thing = JSON_MAPPER.convertValue(map, Thing.class)
        return self.produce(thing)
    }

    static ConsumedThing consume(Wot self, Map map) {
        def thing = JSON_MAPPER.convertValue(map, Thing.class)
        return self.consume(thing)
    }
}