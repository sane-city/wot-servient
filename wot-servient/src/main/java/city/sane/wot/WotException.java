package city.sane.wot;

/**
 * A WotException is thrown by {@link Wot} implementations when errors occur.
 */
public class WotException extends Exception {
    public WotException(String message) {
        super(message);
    }

    public WotException(Throwable cause) {
        super(cause);
    }

    public WotException() {
        super();
    }
}