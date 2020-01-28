package city.sane.wot.scripting;

import city.sane.wot.Wot;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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
    public Future runScript(String script, Wot wot, ExecutorService executorService) throws ScriptingEngineException {
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
                "city.sane.wot.thing.security");
        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(importCustomizer);

        GroovyShell shell = new GroovyShell(binding, config);
        Script groovyScript = shell.parse(script);

        CompletableFuture<Void> completableFuture = new CompletableFuture();
        return executorService.submit(() -> {
            try {
                groovyScript.run();
                completableFuture.complete(null);
            }
            catch (RuntimeException e) {
                completableFuture.completeExceptionally(new ScriptingEngineException(e));
            }
        });
    }
}
