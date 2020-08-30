package city.sane.wot.scripting;

/**
 * A ScriptingEngineException is thrown by Implementations of {@link ScriptingEngine} when errors
 * occur.
 */
@SuppressWarnings({ "java:S110" })
class ScriptingEngineException extends ScriptingException {
    public ScriptingEngineException(Throwable cause) {
        super(cause);
    }
}
