package city.sane.wot.scripting;

/**
 * A ScriptingEngineException is thrown by Implementations of {@link ScriptingEngine} when errors
 * occur.
 */
class ScriptingEngineException extends ScriptingException {
    public ScriptingEngineException(Throwable cause) {
        super(cause);
    }
}
