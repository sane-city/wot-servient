package city.sane.wot.binding.mqtt;

import city.sane.Pair;
import city.sane.RefCountResource;
import city.sane.RefCountResourceException;
import city.sane.wot.ServientDiscoveryIgnore;
import city.sane.wot.binding.ProtocolClientFactory;
import com.typesafe.config.Config;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Creates new {@link MqttProtocolClient} instances.
 */
@ServientDiscoveryIgnore
public class MqttProtocolClientFactory implements ProtocolClientFactory {
    private static final Logger log = LoggerFactory.getLogger(MqttProtocolClientFactory.class);
    private final RefCountResource<Pair<MqttProtocolSettings, MqttClient>> settingsClientPairProvider;
    private Pair<MqttProtocolSettings, MqttClient> settingsClientPair;

    public MqttProtocolClientFactory(Config config) {
        settingsClientPairProvider = SharedMqttClientProvider.singleton(config);
    }

    @Override
    public String getScheme() {
        return "mqtt";
    }

    @Override
    public MqttProtocolClient getClient() {
        return new MqttProtocolClient(settingsClientPair);
    }

    @Override
    public CompletableFuture<Void> init() {
        log.debug("Init MqttClient");

        if (settingsClientPair == null) {
            return runAsync(() -> {
                try {
                    settingsClientPair = settingsClientPairProvider.retain();
                }
                catch (RefCountResourceException e) {
                    throw new CompletionException(e);
                }
            });
        }
        else {
            return completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> destroy() {
        log.debug("Disconnect MqttClient");

        if (settingsClientPair != null) {
            return runAsync(() -> {
                try {
                    settingsClientPairProvider.release();
                }
                catch (RefCountResourceException e) {
                    throw new CompletionException(e);
                }
            });
        }
        else {
            return completedFuture(null);
        }
    }
}
