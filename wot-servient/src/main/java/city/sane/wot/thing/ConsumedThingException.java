package city.sane.wot.thing;

/**
 * A ConsumedThingException is thrown by {@link ConsumedThing} when errors occur.
 */
public class ConsumedThingException extends Exception {
    public ConsumedThingException(String message) {
        super(message);
    }

    public ConsumedThingException(Throwable cause) {
        super(cause);
    }
}