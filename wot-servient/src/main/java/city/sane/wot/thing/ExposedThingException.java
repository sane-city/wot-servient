package city.sane.wot.thing;

/**
 * A ExposedThingException is thrown by {@link ExposedThing} when errors occur.
 */
public class ExposedThingException extends Exception {
    public ExposedThingException(String message) {
        super(message);
    }

    public ExposedThingException(Throwable cause) {
        super(cause);
    }
}
