package city.sane.wot.binding;

/**
 * This exception is thrown when the a {@link ProtocolServer} implementation does not support a
 * requested functionality.
 */
@SuppressWarnings({ "java:S110" })
public class ProtocolServerNotImplementedException extends ProtocolServerException {
    public ProtocolServerNotImplementedException(Class clazz, String operation) {
        super(clazz.getSimpleName() + " does not implement '" + operation + "'");
    }

    public ProtocolServerNotImplementedException(String message) {
        super(message);
    }
}
