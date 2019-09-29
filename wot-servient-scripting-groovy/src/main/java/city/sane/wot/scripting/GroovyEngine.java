package city.sane.wot.scripting;

import city.sane.wot.Wot;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

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
    public void runScript(String script, Wot wot) {
        Binding binding = new Binding();
        binding.setVariable("wot", wot);

        GroovyShell shell = new GroovyShell(binding);
        shell.evaluate(script);
    }
}
