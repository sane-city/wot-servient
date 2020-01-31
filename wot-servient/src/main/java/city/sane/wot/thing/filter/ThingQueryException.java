package city.sane.wot.thing.filter;

import city.sane.wot.ServientException;

/**
 * This exception is thrown when an invalid query is attempted to be used.
 */
public class ThingQueryException extends ServientException {
    public ThingQueryException(Throwable cause) {
        super(cause);
    }

    public ThingQueryException(String message) {
        super(message);
    }
}
