package city.sane.wot.thing.filter;

/**
 * This exception is thrown when an invalid query is attempted to be used.
 */
public class ThingQueryException extends Exception {
    public ThingQueryException(Throwable cause) {
        super(cause);
    }

    public ThingQueryException(String message) {
        super(message);
    }
}
