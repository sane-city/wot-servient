package city.sane.wot.binding.mqtt;

import city.sane.Pair;
import city.sane.RefCountResource;
import com.typesafe.config.Config;
import org.eclipse.paho.client.mqttv3.MqttClient;

/**
 * This is a Singleton class, which is used by {@link MqttProtocolClient} and {@link
 * MqttProtocolServer} to share a single MqttClient.
 */
public class SharedMqttClientProvider {
    private static RefCountResource<Pair<MqttProtocolSettings, MqttClient>> singleton = null;

    private SharedMqttClientProvider() {
        // singleton class
    }

    public static synchronized RefCountResource<Pair<MqttProtocolSettings, MqttClient>> singleton(Config config) {
        if (singleton == null) {
            singleton = new RefCountResource<>(
                    () -> {
                        MqttProtocolSettings settings = new MqttProtocolSettings(config);
                        settings.validate();
                        return new Pair(settings, settings.createConnectedMqttClient());
                    },
                    pair -> pair.second().disconnect()
            );
        }
        return singleton;
    }
}
