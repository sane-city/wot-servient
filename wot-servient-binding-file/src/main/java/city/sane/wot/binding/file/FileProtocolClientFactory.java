package city.sane.wot.binding.file;

import city.sane.wot.binding.ProtocolClientFactory;

/**
 * Creates new {@link FileProtocolClient} instances.
 */
public class FileProtocolClientFactory implements ProtocolClientFactory {
    @Override
    public String getScheme() {
        return "file";
    }

    @Override
    public FileProtocolClient getClient() {
        return new FileProtocolClient();
    }
}
