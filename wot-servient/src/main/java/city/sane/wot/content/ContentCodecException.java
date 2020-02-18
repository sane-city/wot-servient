package city.sane.wot.content;

import city.sane.wot.ServientException;

/**
 * If errors occur during (de)serialization, this exception is thrown.
 */
public class ContentCodecException extends ServientException {
    public ContentCodecException(String message) {
        super(message);
    }

    public ContentCodecException(Throwable cause) {
        super(cause);
    }
}
