package city.sane.wot.content;

/**
 * If errors occur during (de)serialization, this exception is thrown.
 */
public class ContentCodecException extends Exception {
    public ContentCodecException(String message) {
        super(message);
    }

    public ContentCodecException(Throwable cause) {
        super(cause);
    }
}
