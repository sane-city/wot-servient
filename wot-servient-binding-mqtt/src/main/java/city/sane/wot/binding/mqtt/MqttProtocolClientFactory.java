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
