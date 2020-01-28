package city.sane.wot.scripting;

import city.sane.wot.Wot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A ScriptingEngine describes how a WoT script can be executed in a certain scripting language.
 */
interface ScriptingEngine {
    /**
     * Returns the media type supported by the codec (e.g. application/javascript).
     *
     * @return
     */
    String getMediaType();

    /**
     * Returns the file extension supported by the codec (e.g. .js).
     *
     * @return
     */
    String getFileExtension();

    Future runScript(String script, Wot wot, ExecutorService executorService) throws ScriptingEngineException;
}
