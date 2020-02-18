package city.sane.wot.scripting;

import city.sane.wot.ServientException;

public abstract class ScriptingException extends ServientException {
    public ScriptingException(String message) {
        super(message);
    }

    public ScriptingException(Throwable cause) {
        super(cause);
    }
}
