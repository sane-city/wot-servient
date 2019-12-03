package city.sane.wot.cli;

import org.apache.commons.cli.ParseException;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CliTest {
    @Test
    public void help() throws ParseException, CliException {
        new Cli(new String[]{ "--help" });
    }

    @Test
    public void version() throws CliException {
        new Cli(new String[]{ "--version" });
    }

    @Test
    public void runGivenScript() throws CliException {
        new Cli(new String[]{
                "examples/scripts/example-dynamic.groovy"
        });
    }
}