package city.sane.wot.cli;

import org.apache.commons.cli.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import static org.testng.Assert.assertTrue;

@Ignore
public class CliTest {
    @Test
    public void help() throws ParseException, CliException {
        new Cli(new String[]{ "--help" });

        assertTrue(true);
    }

    @Test
    public void version() throws CliException {
        new Cli(new String[]{ "--version" });

        assertTrue(true);
    }

    @Test
    public void runGivenScript() throws CliException {
        new Cli(new String[]{
                "examples/scripts/example-dynamic.groovy"
        });

        assertTrue(true);
    }
}