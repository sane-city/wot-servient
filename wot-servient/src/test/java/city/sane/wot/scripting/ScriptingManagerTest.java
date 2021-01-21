package city.sane.wot.scripting;

import city.sane.wot.Wot;
import com.google.common.io.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
    public void runScript() throws IOException {
        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        File file = folder.newFile("counter.test");
        Files.write("1+1", file, Charset.defaultCharset());

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
    public void runScriptUnsupportedMediaType() {
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
    public void runPrivilegedScript() throws IOException {
        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        File file = folder.newFile("counter.test");
        Files.write("1+1", file, Charset.defaultCharset());

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
    public void runPrivilegedScriptUnsupportedMediaType() {
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