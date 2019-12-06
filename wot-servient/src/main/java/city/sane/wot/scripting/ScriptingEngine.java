package city.sane.wot.scripting;

import city.sane.wot.Wot;

/**
 * A ScriptingEngine describes how a WoT script can be executed in a certain scripting language.
 */
public interface ScriptingEngine {
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

    void runScript(String script, Wot wot) throws ScriptingEngineException;
}
