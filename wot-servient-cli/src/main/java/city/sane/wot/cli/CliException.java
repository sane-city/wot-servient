package city.sane.wot.cli;

import city.sane.wot.ServientException;

/**
 * A CliException is thrown by the {@link Cli} when errors occur.
 */
class CliException extends ServientException {
    public CliException(String message) {
        super(message);
    }

    public CliException(Throwable cause) {
        super(cause);
    }
}