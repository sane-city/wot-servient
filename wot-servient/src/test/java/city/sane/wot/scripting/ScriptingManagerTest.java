package city.sane.wot.scripting;

import city.sane.wot.Wot;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertTrue;

public class ScriptingManagerTest {
    @Before
    public void setUp() {
        ScriptingManager.addEngine(new MyScriptingEngine());
    }

    @Test
    public void runScript() throws ScriptingException, IOException {
        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        File file = folder.newFile("counter.test");
        Files.write("1+1", file, Charset.defaultCharset());

        ScriptingManager.runScript(file,null);

        // should not fail
        assertTrue(true);
    }

    @Test
    public void runScriptString() throws ScriptingException {
        ScriptingManager.runScript("1+1", "application/test",null);

        // should not fail
        assertTrue(true);
    }

    @Test(expected = ScriptingException.class)
    public void runScriptUnsupportedMediaType() throws ScriptingException {
        ScriptingManager.runScript("1+1", "application/lolcode",null);
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
        public void runScript(String script, Wot wot) {

        }
    }
}