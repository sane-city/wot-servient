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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScriptingManagerTest {
    @BeforeEach
    public void setUp() {
        ScriptingManager.addEngine(new MyScriptingEngine());
    }

    @Test
    public void runScript(@TempDir Path folder) throws IOException {
        File file = Paths.get(folder.toString(), "counter.test").toFile();
        java.nio.file.Files.writeString(file.toPath(), "1+1");

        ScriptingManager.runScript(file, null);

        // should not fail
        assertTrue(true);
    }

    @Test
    public void runScriptString() throws ExecutionException, InterruptedException {
        ScriptingManager.runScript("1+1", "application/test", null).get();

        // should not fail
        assertTrue(true);
    }

    @Test
    public void runScriptUnsupportedMediaType() throws Throwable {
        assertThrows(ScriptingException.class, () -> {
            try {
                ScriptingManager.runScript("1+1", "application/lolcode", null).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void runPrivilegedScript(@TempDir Path folder) throws IOException {
        File file = Paths.get(folder.toString(), "counter.test").toFile();
        Files.writeString(file.toPath(), "1+1");

        ScriptingManager.runPrivilegedScript(file, null);

        // should not fail
        assertTrue(true);
    }

    @Test
    public void runPrivilegedScriptString() throws ExecutionException, InterruptedException {
        ScriptingManager.runPrivilegedScript("1+1", "application/test", null).get();

        // should not fail
        assertTrue(true);
    }

    @Test
    public void runPrivilegedScriptUnsupportedMediaType() throws Throwable {
        assertThrows(ScriptingException.class, () -> {
            try {
                ScriptingManager.runPrivilegedScript("1+1", "application/lolcode", null).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    static class MyScriptingEngine implements ScriptingEngine {
        @Override
        public String getMediaType() {
            return "application/test";
        }

        @Override
        public String getFileExtension() {
            return ".test";
        }

        @Override
        public CompletableFuture<Void> runScript(String script,
                                                 Wot wot,
                                                 ExecutorService executorService) {
            return completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> runPrivilegedScript(String script,
                                                           Wot wot,
                                                           ExecutorService executorService) {
            return completedFuture(null);
        }
    }
}