package city.sane.wot.cli;

/**
 * This exception is thrown when the command line interface should terminate.
 */
public class CliShutdownException extends RuntimeException {
    public CliShutdownException() {
        super();
    }
}
