package city.sane.wot.binding;

/**
 * This exception is thrown when the a {@link ProtocolClient} implementation does not support a
 * requested functionality.
 */
public class ProtocolClientNotImplementedException extends ProtocolClientException {
    public ProtocolClientNotImplementedException(Class clazz, String operation) {
        super(clazz.getSimpleName() + " does not implement '" + operation + "'");
    }

    public ProtocolClientNotImplementedException(String message) {
        super(message);
    }
}
