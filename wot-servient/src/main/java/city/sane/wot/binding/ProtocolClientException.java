package city.sane.wot.binding;

import city.sane.wot.ServientException;

/**
 * A ProtocolClientException is thrown by {@link ProtocolClient} implementations when errors occur.
 */
public class ProtocolClientException extends ServientException {
    public ProtocolClientException(String message) {
        super(message);
    }

    public ProtocolClientException(Throwable cause) {
        super(cause);
    }
}
