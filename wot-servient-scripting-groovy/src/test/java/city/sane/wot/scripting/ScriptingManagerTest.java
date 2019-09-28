package city.sane.wot.scripting;

import city.sane.wot.DefaultWot;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class ScriptingManagerTest {
    @Before
    public void setup() {
        ScriptingManager.addEngine(new GroovyEngine());
    }

    @Test
    public void runScript() throws ScriptingManagerException {
        ScriptingManager.runScript(new File("counter.groovy"), new DefaultWot());
    }
}