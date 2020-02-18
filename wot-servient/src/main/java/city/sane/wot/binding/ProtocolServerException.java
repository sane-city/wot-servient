package city.sane.wot.binding;

import city.sane.wot.ServientException;

/**
 * A ProtocolServerException is thrown by {@link ProtocolServer} implementations when errors occur.
 */
public class ProtocolServerException extends ServientException {
    public ProtocolServerException(String message) {
        super(message);
    }

    public ProtocolServerException(Throwable cause) {
        super(cause);
    }

    public ProtocolServerException() {
        super();
    }
}
