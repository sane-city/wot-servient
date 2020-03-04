package city.sane.wot.cli;

import city.sane.wot.Servient;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CliIT {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void helpShouldPrintHelp() throws CliException {
        new Cli(new String[]{ "--help" });

        assertThat(outContent.toString(), containsString("Usage:"));
    }

    @Test
    public void versionShouldPrintVersion() throws CliException {
        new Cli(new String[]{ "--version" });

        assertThat(outContent.toString(), containsString(Servient.getVersion()));
    }

    @Test
    public void shouldPrintOutputFromScript() throws CliException, IOException {
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

        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        File file = folder.newFile("my-thing.groovy");
        Files.write(script, file, Charset.defaultCharset());

        new Cli(new String[]{
                "--clientonly",
                file.getAbsolutePath()
        });

        assertThat(outContent.toString(), containsString("KlimabotschafterWetterstation"));
    }

    @Test
    public void shouldPrintErrorOfBrokenScript() throws IOException {
        String script = "1/0";

        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        File file = folder.newFile("my-thing.groovy");
        Files.write(script, file, Charset.defaultCharset());

        assertThrows(CliException.class, () -> {
            new Cli(new String[]{
                    file.getAbsolutePath()
            });
        });
    }
}