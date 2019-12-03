package city.sane.wot.cli;

/**
 * A CliException is thrown by the {@link Cli} when errors occur.
 */
public class CliException extends Exception {
    public CliException(String message) {
        super(message);
    }

    public CliException(Throwable cause) {
        super(cause);
    }
}