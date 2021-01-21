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

import city.sane.wot.Wot;
import city.sane.wot.thing.Thing;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

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
    public CompletableFuture<Void> runScript(String script,
                                             Wot wot,
                                             ExecutorService executorService) {
        CompilerConfiguration config = getCompilerConfiguration();

        CompletableFuture<Void> completionFuture = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                Binding binding = new Binding();
                binding.setVariable("wot", wot);
                GroovyShell shell = new GroovyShell(binding, config);

                Script groovyScript = shell.parse(script);
                groovyScript.run();

                completionFuture.complete(null);
            }
            catch (RuntimeException e) {
                completionFuture.completeExceptionally(new ScriptingEngineException(e));
            }
        });

        return completionFuture;
    }

    @Override
    public CompletableFuture<Void> runPrivilegedScript(String script,
                                                       Wot wot,
                                                       ExecutorService executorService) {
        Binding binding = new Binding();
        binding.setVariable("wot", wot);

        CompilerConfiguration config = getPrivilegedCompilerConfiguration();

        GroovyShell shell = new GroovyShell(binding, config);
        Script groovyScript = shell.parse(script);

        CompletableFuture<Void> completionFuture = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                groovyScript.run();
                completionFuture.complete(null);
            }
            catch (RuntimeException e) {
                completionFuture.completeExceptionally(new ScriptingEngineException(e));
            }
        });

        return completionFuture;
    }

    private CompilerConfiguration getPrivilegedCompilerConfiguration() {
        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(getImportCustomizer());
        return config;
    }

    private CompilerConfiguration getCompilerConfiguration() {
        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(getImportCustomizer());
        config.addCompilationCustomizers(getSecureASTCustomizer());
        return config;
    }

    private ImportCustomizer getImportCustomizer() {
        ImportCustomizer customizer = new ImportCustomizer();
        customizer.addImports(
                "city.sane.wot.thing.Thing"
        );
        return customizer;
    }

    @SuppressWarnings("squid:S125")
    private SecureASTCustomizer getSecureASTCustomizer() {
        SecureASTCustomizer customizer = new SecureASTCustomizer();
        customizer.setMethodDefinitionAllowed(false);
        customizer.setClosuresAllowed(false);
        customizer.setPackageAllowed(false);
        customizer.setImportsWhitelist(List.of(
                // java.lang
                Boolean.class.getName(),
                Byte.class.getName(),
                Character.class.getName(),
                Double.class.getName(),
                Exception.class.getName(),
                Float.class.getName(),
                Integer.class.getName(),
                Long.class.getName(),
                Math.class.getName(),
                Number.class.getName(),
                Short.class.getName(),
                String.class.getName(),
                StringBuilder.class.getName(),
                // java.util
                ArrayList.class.getName(),
                Arrays.class.getName(),
                Collections.class.getName(),
                Date.class.getName(),
                Formatter.class.getName(),
                HashMap.class.getName(),
                HashSet.class.getName(),
                LinkedHashMap.class.getName(),
                LinkedHashSet.class.getName(),
                LinkedList.class.getName(),
                List.class.getName(),
                Map.class.getName(),
                Objects.class.getName(),
                Queue.class.getName(),
                Set.class.getName(),
                SortedMap.class.getName(),
                SortedSet.class.getName(),
                TreeMap.class.getName(),
                TreeSet.class.getName(),
                UUID.class.getName(),
                // city.sane.wot.thing
                Thing.class.getName()
        ));
//        customizer.setStarImportsWhitelist(List.of());
//        customizer.setStaticImportsWhitelist(List.of());
//        customizer.setStaticStarImportsWhitelist(List.of());
//        customizer.setExpressionsWhitelist(List.of());
//        customizer.setStatementsWhitelist(List.of());
        customizer.setStatementsBlacklist(List.of(DoWhileStatement.class, ForStatement.class, WhileStatement.class));
        customizer.setIndirectImportCheckEnabled(true);
//        customizer.setTokensWhitelist(List.of());
//        customizer.setConstantTypesClassesWhiteList(List.of());
        customizer.setReceiversClassesBlackList(List.of());
        return customizer;
    }
}
