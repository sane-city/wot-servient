package city.sane.wot.scripting;

import city.sane.wot.DefaultWot;
import city.sane.wot.WotException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GroovyEngineTest {
    private ScriptingEngine engine;
    private ExecutorService executorService;

    @BeforeEach
    public void setup() {
        engine = new GroovyEngine();
        executorService = Executors.newCachedThreadPool();
    }

    @AfterEach
    public void teardown() {
        executorService.shutdown();
    }

    @Test
    public void runPrivilegedScriptShouldNotFailOnValidScript() throws WotException, ExecutionException, InterruptedException {
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
        engine.runPrivilegedScript(script, wot, executorService).get();

        // should not fail
        assertTrue(true);
    }

    @Test
    public void runPrivilegedScriptShouldFailOnInvalidScript() throws Throwable {
        String script = "wot.dahsjkdhajkdhajkdhasjk()";

        DefaultWot wot = new DefaultWot();
        assertThrows(ScriptingEngineException.class, () -> {
            try {
                engine.runPrivilegedScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void runPrivilegedScriptShouldAutoImportServientClasses() throws WotException, ExecutionException, InterruptedException {
        String script = "new Thing()";

        DefaultWot wot = new DefaultWot();
        engine.runPrivilegedScript(script, wot, executorService).get();

        // should not fail
        assertTrue(true);
    }

    @Test
    public void runScriptShouldAutoImportServientClasses() throws WotException, ExecutionException, InterruptedException {
        String script = "new Thing()";

        DefaultWot wot = new DefaultWot();
        engine.runScript(script, wot, executorService).get();

        // should not fail
        assertTrue(true);
    }

    @Test
    public void runScriptShouldBlockMaliciousScripts() throws Throwable {
        DefaultWot wot = new DefaultWot();

        assertThrows(ScriptingEngineException.class, () -> {
            try {
                String script = "System.exit(1)";
                engine.runScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });

        assertThrows(ScriptingEngineException.class, () -> {
            try {
                String script = "Eval.me(\"System.exit(1)\")";
                engine.runScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });

        assertThrows(ScriptingEngineException.class, () -> {
            try {
                String script = "evaluate(\"System.exit(1)\")";
                engine.runScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });

        assertThrows(ScriptingEngineException.class, () -> {
            try {
                String script = "(new GroovyShell()).evaluate(\"System.exit(1)\")";
                engine.runScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });

        assertThrows(ScriptingEngineException.class, () -> {
            try {
                String script = "Class.forName(\"java.lang.System\").exit(1)";
                engine.runScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });

        assertThrows(ScriptingEngineException.class, () -> {
            try {
                String script = "System.&exit.call(1)";
                engine.runScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });

        assertThrows(ScriptingEngineException.class, () -> {
            try {
                String script = "System.getMetaClass().invokeMethod(\"exit\", 1)";
                engine.runScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });

        assertThrows(ScriptingEngineException.class, () -> {
            try {
                String script = "def s = System; s.exit(1)";
                engine.runScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });

        assertThrows(ScriptingEngineException.class, () -> {
            try {
                String script = "Script t = this; t.evaluate(\"System.exit(1)\")";
                engine.runScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });

        assertThrows(ScriptingEngineException.class, () -> {
            try {
                String script = "Math.class.forName(\"java.lang.System\").getMethod(\"exit\", Integer.TYPE).invoke" +
                        "(null, -1);";
                engine.runScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });

        assertThrows(ScriptingEngineException.class, () -> {
            try {
                String script = "5[\"class\"].forName(\"java.lang.System\").getMethod(\"exit\", Integer.TYPE).invoke" +
                        "(null, -1);";
                engine.runScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });

        assertThrows(ScriptingEngineException.class, () -> {
            try {
                String script = "StringBuilder sb = new StringBuilder();\n" +
                        "        sb.append(\"ss\");\n" +
                        "        sb.append(\"a\");\n" +
                        "        sb.append(\"l\");\n" +
                        "        \n" +
                        "        1[\"c\"+sb.reverse().toString()].forName(\"java.lang.System\").getMethod(\"exit\", " +
                        "Integer.TYPE).invoke" +
                        "(null, 3);";
                engine.runScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });

        assertThrows(ScriptingEngineException.class, () -> {
            try {
                String script = "new File(\"file.txt\").createNewFile()";
                engine.runScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });

        assertThrows(ScriptingEngineException.class, () -> {
            try {
                String script = "new DefaultWot()";
                engine.runScript(script, wot, executorService).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });
    }
}