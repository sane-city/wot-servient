package city.sane.wot.thing;

import city.sane.wot.thing.form.Operation;

public class NoFormForInteractionConsumedThingException extends ConsumedThingException {
    public NoFormForInteractionConsumedThingException(String title, Operation op) {
        super("'" + title + "' has no form for interaction '" + op + "'");
    }
}
