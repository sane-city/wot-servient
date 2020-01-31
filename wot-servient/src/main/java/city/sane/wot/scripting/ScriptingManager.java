package city.sane.wot.scripting;

import city.sane.wot.Wot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.CompletableFuture.failedFuture;

/**
 * The ScriptingManager executes WoT scripts in certain scripting languages.
 */
public class ScriptingManager {
    private final static ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private static final Map<String, ScriptingEngine> ENGINES = new HashMap();

    private ScriptingManager() {
    }

    /**
     * Adds support for the script language specified in <code>engine</code>.
     *
     * @param engine
     */
    public static void addEngine(ScriptingEngine engine) {
        ENGINES.put(engine.getMediaType(), engine);
    }

    /**
     * Removes support for the script language specified in <code>extension</code>.
     *
     * @param extension
     */
    public static void removeEngine(String extension) {
        ENGINES.remove(extension);
    }

    /**
     * Executes the WoT script in <code>file</code> and passes <code>wot</code> to the script as WoT object.
     *
     * @param file
     * @param wot
     * @throws ScriptingManagerException
     * @return
     */
    public static CompletableFuture<Void> runScript(File file, Wot wot) {
        Path path = file.toPath();
        try {
            String script = Files.readString(path);
            String extension = pathToExtension(path);
            String mediaType = extensionToMediaType(extension);

            if (mediaType == null) {
                return failedFuture(new ScriptingManagerException("No scripting engine available for extension '" + extension + "'"));
            }

            return runScript(script, mediaType, wot);
        }
        catch (IOException e) {
            return failedFuture(new ScriptingManagerException(e));
        }
    }

    /**
     * Executes the WoT script in <code>script</code> using engine that matches <code>mediaType</code> and passes <code>wot</code> to the script as WoT object.
     *
     * @param script
     * @param mediaType
     * @param wot
     * @throws ScriptingManagerException
     * @return
     */
    public static CompletableFuture<Void> runScript(String script, String mediaType, Wot wot) {
        ScriptingEngine engine = ENGINES.get(mediaType);

        if (engine == null) {
            return failedFuture(new ScriptingManagerException("No scripting engine available for media type '" + mediaType + "'"));
        }

        return engine.runScript(script, wot, EXECUTOR_SERVICE);
    }

    private static String pathToExtension(Path path) {
        String pathStr = path.toString();
        if (pathStr.contains(".")) {
            return pathStr.substring(pathStr.lastIndexOf('.'));
        }
        else {
            return null;
        }
    }

    private static String extensionToMediaType(String extension) {
        Optional<ScriptingEngine> engine = ENGINES.values().stream().filter(e -> e.getFileExtension().equals(extension)).findFirst();
        return engine.map(ScriptingEngine::getMediaType).orElse(null);
    }
}
