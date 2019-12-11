package city.sane.wot.thing;

import java.util.Set;

class NoClientFactoryForSchemesConsumedThingException extends ConsumedThingException {
    public NoClientFactoryForSchemesConsumedThingException(String title, Set<String> schemes) {
        super("'" + title + "': Missing ClientFactory for schemes '" + schemes + "'");
    }
}