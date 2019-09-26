package city.sane.wot


import city.sane.wot.Wot
import city.sane.wot.thing.ConsumedThing
import city.sane.wot.thing.ExposedThing
import city.sane.wot.thing.Thing
import com.fasterxml.jackson.databind.ObjectMapper

class WotExtensions {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    static ExposedThing produce(Wot self, Map map) {
        def thing = JSON_MAPPER.convertValue(map, Thing.class)
        return self.produce(thing)
    }

    static ConsumedThing consume(Wot self, Map map) {
        def thing = JSON_MAPPER.convertValue(map, Thing.class)
        return self.consume(thing)
    }
}