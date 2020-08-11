package city.sane.wot.binding.mqtt;

import city.sane.Pair;
import city.sane.RefCountResource;
import com.typesafe.config.Config;
import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a Singleton class, which is used by {@link MqttProtocolClient} and {@link
 * MqttProtocolServer} to share a single MqttClient.
 */
public class SharedMqttClientProvider {
    private static final Map<Config, RefCountResource<Pair<MqttProtocolSettings, MqttClient>>> singletons = new HashMap<>();

    private SharedMqttClientProvider() {
        // singleton class
    }

    public static synchronized RefCountResource<Pair<MqttProtocolSettings, MqttClient>> singleton(
            Config config) {
        return singletons.computeIfAbsent(
                config,
                myConfig -> new RefCountResource<>(
                        () -> {
                            MqttProtocolSettings settings = new MqttProtocolSettings(myConfig);
                            settings.validate();
                            return new Pair<>(settings, settings.createConnectedMqttClient());
                        },
                        pair -> pair.second().disconnect()
                )
        );
    }
}
