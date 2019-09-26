package city.sane.wot;

/**
 * A ServientException is thrown by the {@link Servient} when errors occur.
 */
public class ServientException extends Exception {
    public ServientException(String message) {
        super(message);
    }

    public ServientException(Throwable cause) {
        super(cause);
    }
}