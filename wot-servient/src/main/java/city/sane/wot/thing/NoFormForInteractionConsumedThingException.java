package city.sane.wot.thing;

import city.sane.wot.thing.form.Operation;

class NoFormForInteractionConsumedThingException extends ConsumedThingException {
    public NoFormForInteractionConsumedThingException(String title, Operation op) {
        super("'" + title + "' has no form for interaction '" + op + "'");
    }

    public NoFormForInteractionConsumedThingException(String message) {
        super(message);
    }
}
