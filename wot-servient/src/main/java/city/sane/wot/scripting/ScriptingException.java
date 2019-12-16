package city.sane.wot.scripting;

public abstract class ScriptingException extends Exception {
    public ScriptingException(String message) {
        super(message);
    }

    public ScriptingException(Throwable cause) {
        super(cause);
    }
}
