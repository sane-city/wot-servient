package city.sane.wot.scripting;

/**
 * A ScriptingManagerException is thrown by {@link ScriptingManager} when errors occur.
 */
@SuppressWarnings({ "java:S110" })
class ScriptingManagerException extends ScriptingException {
    public ScriptingManagerException(String message) {
        super(message);
    }

    public ScriptingManagerException(Throwable cause) {
        super(cause);
    }
}
