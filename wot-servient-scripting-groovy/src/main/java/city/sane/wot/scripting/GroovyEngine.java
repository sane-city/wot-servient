package city.sane.wot.scripting;

import city.sane.wot.Wot;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

/**
 * Allows the execution of WoT scripts written in the programming language Groovy.
 */
public class GroovyEngine implements ScriptingEngine {
    @Override
    public String getMediaType() {
        return "application/groovy";
    }

    @Override
    public String getFileExtension() {
        return ".groovy";
    }

    @Override
    public void runScript(String script, Wot wot) throws ScriptingEngineException {
        Binding binding = new Binding();
        binding.setVariable("wot", wot);

        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStarImports(
                "city.sane.wot",
                "city.sane.wot.binding",
                "city.sane.wot.content",
                "city.sane.wot.scripting",
                "city.sane.wot.thing",
                "city.sane.wot.thing.action",
                "city.sane.wot.thing.event",
                "city.sane.wot.thing.filter",
                "city.sane.wot.thing.form",
                "city.sane.wot.thing.property",
                "city.sane.wot.thing.schema",
                "city.sane.wot.thing.security"
        );
        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(importCustomizer);

        GroovyShell shell = new GroovyShell(binding, config);

        try {
            shell.evaluate(script);
        }
        catch (RuntimeException e) {
            throw new ScriptingEngineException(e);
        }
    }
}
