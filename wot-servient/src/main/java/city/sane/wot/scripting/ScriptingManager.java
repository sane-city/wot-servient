package city.sane.wot.scripting;

import city.sane.wot.Wot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The ScriptingManager executes WoT scripts in certain scripting languages.
 */
public class ScriptingManager {
    private static final Map<String, ScriptingEngine> ENGINES = new HashMap();

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
     */
    public static void runScript(File file, Wot wot) throws ScriptingManagerException {
        Path path = file.toPath();
        try {
            String script = Files.readString(path);
            String extension = pathToExtension(path);
            String mediaType = extensionToMediaType(extension);

            if (mediaType == null) {
                throw new ScriptingManagerException("No scripting engine available for extension '" + extension + "'");
            }

            runScript(script, mediaType, wot);
        }
        catch (IOException e) {
            throw new ScriptingManagerException(e);
        }
    }

    /**
     * Executes the WoT script in <code>script</code> using engine that matches <code>mediaType</code> and passes <code>wot</code> to the script as WoT object.
     *
     * @param script
     * @param mediaType
     * @param wot
     * @throws ScriptingManagerException
     */
    public static void runScript(String script, String mediaType, Wot wot) throws ScriptingManagerException {
        ScriptingEngine engine = ENGINES.get(mediaType);

        if (engine == null) {
            throw new ScriptingManagerException("No scripting engine available for media type '" + mediaType + "'");
        }

        engine.runScript(script, wot);
    }

    private static String pathToExtension(Path path) {
        String pathStr = path.toString();
        if (pathStr.contains(".")) {
            return pathStr.substring(pathStr.lastIndexOf("."));
        }
        else {
            return null;
        }
    }

    private static String extensionToMediaType(String extension) {
        Optional<ScriptingEngine> engine = ENGINES.values().stream().filter(e -> e.getFileExtension().equals(extension)).findFirst();
        if (engine.isPresent()) {
            return engine.get().getMediaType();
        }
        else {
            return null;
        }
    }
}
