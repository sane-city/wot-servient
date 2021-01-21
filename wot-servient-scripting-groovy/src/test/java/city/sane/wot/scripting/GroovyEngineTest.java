/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package city.sane.wot.scripting;

import city.sane.wot.DefaultWot;
import city.sane.wot.WotException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void runPrivilegedScriptShouldFailOnInvalidScript() {
        String script = "wot.dahsjkdhajkdhajkdhasjk()";

        assertThrows(ScriptingEngineException.class, () -> {
            DefaultWot wot = new DefaultWot();
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