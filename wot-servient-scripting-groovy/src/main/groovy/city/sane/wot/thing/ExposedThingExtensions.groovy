package city.sane.wot.thing

import city.sane.wot.thing.ExposedThing
import city.sane.wot.thing.action.ThingAction
import city.sane.wot.thing.event.ThingEvent
import city.sane.wot.thing.property.ThingProperty
import com.fasterxml.jackson.databind.ObjectMapper

import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction

class ExposedThingExtensions {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

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
