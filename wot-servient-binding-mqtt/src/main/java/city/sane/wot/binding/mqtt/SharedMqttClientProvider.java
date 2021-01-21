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
