package city.sane.wot.scripting;

/**
 * A ScriptingManagerException is thrown by {@link ScriptingManager} when errors occur.
 */
public class ScriptingManagerException extends Exception {
    public ScriptingManagerException(String message) {
        super(message);
    }

    public ScriptingManagerException(Throwable cause) {
        super(cause);
    }
}
