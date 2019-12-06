package city.sane.wot.scripting;

/**
 * A ScriptingEngineException is thrown by Implementations of {@link ScriptingEngine} when errors occur.
 */
public class ScriptingEngineException extends ScriptingException {
    public ScriptingEngineException(String message) {
        super(message);
    }

    public ScriptingEngineException(Throwable cause) {
        super(cause);
    }
}
