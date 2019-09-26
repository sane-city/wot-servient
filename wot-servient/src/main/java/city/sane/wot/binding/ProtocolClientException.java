package city.sane.wot.binding;

/**
 * A ProtocolClientException is thrown by {@link ProtocolClient} implementations when errors occur.
 */
public class ProtocolClientException extends Exception {
    public ProtocolClientException(String message) {
        super(message);
    }

    public ProtocolClientException(Throwable cause) {
        super(cause);
    }
}
