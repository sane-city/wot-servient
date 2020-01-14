package city.sane.wot;

/**
 * A ServientConfigException is thrown by the {@link ServientConfig} when errors occur.
 */
public class ServientConfigException extends ServientException {
    public ServientConfigException(Exception cause) {
        super(cause);
    }
}
