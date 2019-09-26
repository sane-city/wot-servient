package city.sane.wot.binding.file;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientFactory;
import com.typesafe.config.Config;

/**
 * Creates new {@link FileProtocolClient} instances.
 */
public class FileProtocolClientFactory implements ProtocolClientFactory {
    public FileProtocolClientFactory(Config config) {
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
