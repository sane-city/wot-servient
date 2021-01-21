/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package city.sane.wot.binding.mqtt;

import com.typesafe.config.Config;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MqttProtocolSettings {
    private static final Logger log = LoggerFactory.getLogger(MqttProtocolSettings.class);
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
            // generate random client id
            clientId = "wot" + System.nanoTime();
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

    MqttProtocolSettings(String broker, String clientId, String username, String password) {
        this.broker = broker;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
    }

    public void validate() throws MqttProtocolException {
        if (getBroker() == null || getBroker().isEmpty()) {
            throw new MqttProtocolException("No broker defined for MQTT server binding - skipping");
        }
    }

    public String getBroker() {
        return broker;
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

    public String getClientId() {
        return clientId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
