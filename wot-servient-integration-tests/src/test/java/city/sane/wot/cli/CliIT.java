package city.sane.wot.cli;

import city.sane.wot.Servient;
import com.google.common.io.Files;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CliIT {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private Cli cli;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);

        if (cli != null) {
            cli.shutdown();
        }
    }

    @Test
    public void runShouldPrintHelp() throws CliException {
        cli = new Cli();
        cli.run(new String[]{ "--help" });

        MatcherAssert.assertThat(outContent.toString(), containsString("Usage:"));
    }

    @Test
    public void runShouldPrintVersion() throws CliException {
        cli = new Cli();
        cli.run(new String[]{ "--version" });

        MatcherAssert.assertThat(outContent.toString(), containsString(Servient.getVersion()));
    }

    @Test
    public void runShouldPrintOutputFromScript(@TempDir Path folder) throws CliException, IOException {
        String script = "def thing = [\n" +
                "    id        : 'KlimabotschafterWetterstation',\n" +
                "    title     : 'KlimabotschafterWetterstation',\n" +
                "    '@type'   : 'Thing',\n" +
                "    '@context': [\n" +
                "        'http://www.w3.org/ns/td',\n" +
                "        [\n" +
                "            om   : 'http://www.wurvoc.org/vocabularies/om-1.8/',\n" +
                "            saref: 'https://w3id.org/saref#',\n" +
                "            sch  : 'http://schema.org/',\n" +
                "            sane : 'https://sane.city/',\n" +
                "        ]\n" +
                "    ],\n" +
                "]\n" +
                "\n" +
                "def exposedThing = wot.produce(thing)\n" +
                "\n" +
                "exposedThing.addProperty(\n" +
                "    'Temp_2m',\n" +
                "    [\n" +
                "        '@type'             : 'saref:Temperature',\n" +
                "        description         : 'Temperatur in 2m in Grad Celsisus',\n" +
                "        'om:unit_of_measure': 'om:degree_Celsius',\n" +
                "        type                : 'number',\n" +
                "        readOnly            : true,\n" +
                "        observable          : true\n" +
                "    ]\n" +
                ")\n" +
                "println(exposedThing.toJson(true))";

        File file = Paths.get(folder.toString(), "my-thing.groovy").toFile();
        Files.write(script, file, Charset.defaultCharset());

        cli = new Cli();
        cli.run(new String[]{
                "--clientonly",
                file.getAbsolutePath()
        });

        MatcherAssert.assertThat(outContent.toString(), containsString("KlimabotschafterWetterstation"));
    }

    @Test
    public void shouldPrintErrorOfBrokenScript(@TempDir Path folder) throws IOException {
        String script = "1/0";

        File file = Paths.get(folder.toString(), "counter.json").toFile();
        Files.write(script, file, Charset.defaultCharset());

        assertThrows(CliException.class, () -> {
            cli = new Cli();
            cli.run(new String[]{
                    file.getAbsolutePath()
            });
        });
    }
}