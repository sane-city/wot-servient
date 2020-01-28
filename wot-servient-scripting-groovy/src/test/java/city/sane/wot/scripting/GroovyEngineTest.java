package city.sane.wot.scripting;

import city.sane.wot.DefaultWot;
import city.sane.wot.WotException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class GroovyEngineTest {
    private ScriptingEngine engine;
    private ExecutorService executorService;

    @Before
    public void setup() {
        engine = new GroovyEngine();
        executorService = Executors.newCachedThreadPool();
    }

    @After
    public void teardown() {
        executorService.shutdown();
    }

    @Test
    public void runScript() throws ScriptingException, WotException {
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

        DefaultWot wot = new DefaultWot();
        engine.runScript(script, wot, executorService);

        // should not fail
        assertTrue(true);
    }

    @Test(expected = ScriptingEngineException.class)
    public void runInvalidScript() throws ScriptingException, WotException {
        String script = "wot.dahsjkdhajkdhajkdhasjk()";

        DefaultWot wot = new DefaultWot();
        engine.runScript(script, wot, executorService);
    }

    @Test
    public void runScriptWithDefaultImport() throws ScriptingException, WotException {
        String script = "new Thing()";

        DefaultWot wot = new DefaultWot();
        engine.runScript(script, wot, executorService);
    }
}