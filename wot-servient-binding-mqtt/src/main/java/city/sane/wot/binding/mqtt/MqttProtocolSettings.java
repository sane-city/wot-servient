package city.sane.wot.binding.mqtt;

import com.typesafe.config.Config;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttProtocolSettings {
    static final Logger log = LoggerFactory.getLogger(MqttProtocolSettings.class);

    private final String broker;
    private final String clientId;
    private final String username;
    private final String password;

    public MqttProtocolSettings(Config config) {
        if (config.hasPath("wot.servient.mqtt.broker")) {
            broker = config.getString("wot.servient.mqtt.broker");
        }
        else {
            broker = null;
        }

        if (config.hasPath("wot.servient.mqtt.client-id")) {
            clientId = config.getString("wot.servient.mqtt.client-id");
        }
        else {
            clientId = MqttClient.generateClientId();
        }

        if (config.hasPath("wot.servient.mqtt.username")) {
            username = config.getString("wot.servient.mqtt.username");
        }
        else {
            username = null;
        }

        if (config.hasPath("wot.servient.mqtt.password")) {
            password = config.getString("wot.servient.mqtt.password");
        }
        else {
            password = null;
        }
    }

    public String getBroker() {
        return broker;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void validate() throws MqttProtocolException {
        if (getBroker() == null || getBroker().isEmpty()) {
            throw new MqttProtocolException("No broker defined for MQTT server binding - skipping");
        }
    }

    public MqttClient createConnectedMqttClient() throws MqttProtocolException {
        try (MqttClientPersistence persistence = new MemoryPersistence()) {
            MqttClient client = new MqttClient(getBroker(), getClientId(), persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            if (getUsername() != null) {
                options.setUserName(getUsername());
            }
            if (getPassword() != null) {
                options.setPassword(getPassword().toCharArray());
            }

            log.info("MqttClient trying to connect to broker at '{}' with client ID '{}'", getBroker(), getClientId());
            client.connect(options);
            log.info("MqttClient connected to broker at '{}'", getBroker());

            return client;
        }
        catch (MqttException e) {
            throw new MqttProtocolException(e);
        }
    }
}
