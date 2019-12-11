package city.sane.wot.binding.file;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientFactory;

/**
 * Creates new {@link FileProtocolClient} instances.
 */
public class FileProtocolClientFactory implements ProtocolClientFactory {
    public FileProtocolClientFactory() {
    }

    @Override
    public String getScheme() {
        return "file";
    }

    @Override
    public ProtocolClient getClient() {
        return new FileProtocolClient();
    }
}
