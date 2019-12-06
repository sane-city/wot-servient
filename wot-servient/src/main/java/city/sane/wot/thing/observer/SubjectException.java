package city.sane.wot.thing.observer;

/**
 * Will be thrown if interaction with a {@link Subject} is faulty (e.g. if an attempt is made to send new values to a closed subject).
 */
public abstract class SubjectException extends Exception {
    public SubjectException(String message) {
        super(message);
    }

    public SubjectException(Throwable cause) {
        super(cause);
    }
}
