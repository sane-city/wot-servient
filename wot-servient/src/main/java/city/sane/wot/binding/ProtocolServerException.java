package city.sane.wot.binding;

/**
 * A ProtocolServerException is thrown by {@link ProtocolServer} implementations when errors occur.
 */
public class ProtocolServerException extends Exception {
    public ProtocolServerException(String message) {
        super(message);
    }

    public ProtocolServerException(Throwable cause) {
        super(cause);
    }
}
