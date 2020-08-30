package city.sane.wot.thing;

import java.util.Set;

@SuppressWarnings({ "java:S110" })
class NoClientFactoryForSchemesConsumedThingException extends ConsumedThingException {
    public NoClientFactoryForSchemesConsumedThingException(String title, Set<String> schemes) {
        super("'" + title + "': Missing ClientFactory for schemes '" + schemes + "'");
    }
}