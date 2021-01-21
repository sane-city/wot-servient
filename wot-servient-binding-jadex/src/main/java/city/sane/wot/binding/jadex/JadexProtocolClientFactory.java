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
package city.sane.wot.binding.jadex;

import city.sane.RefCountResource;
import city.sane.RefCountResourceException;
import city.sane.wot.binding.ProtocolClientFactory;
import com.typesafe.config.Config;
import jadex.bridge.IExternalAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Creates new {@link JadexProtocolClient} instances. A Jadex Platform is created for this
 * purpose.<br> The Jadex Platform can be configured via the configuration parameter
 * "wot.servient.jadex.client".
 */
public class JadexProtocolClientFactory implements ProtocolClientFactory {
    private static final Logger log = LoggerFactory.getLogger(JadexProtocolClientFactory.class);
    private final RefCountResource<IExternalAccess> platformProvider;
    private IExternalAccess platform = null;

    public JadexProtocolClientFactory(Config wotConfig) {
        this(SharedPlatformProvider.singleton(wotConfig));
    }

    public JadexProtocolClientFactory(RefCountResource<IExternalAccess> platformProvider) {
        this.platformProvider = platformProvider;
    }

    @Override
    public String toString() {
        return "JadexClient";
    }

    @Override
    public String getScheme() {
        return "jadex";
    }

    @Override
    public JadexProtocolClient getClient() {
        return new JadexProtocolClient(platform);
    }

    @Override
    public CompletableFuture<Void> init() {
        log.debug("Create Jadex Platform");
        return runAsync(() -> {
            try {
                platform = platformProvider.retain();
            }
            catch (RefCountResourceException e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> destroy() {
        log.debug("Kill Jadex Platform");

        if (platform != null) {
            return runAsync(() -> {
                try {
                    platformProvider.release();
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

    public IExternalAccess getJadexPlatform() {
        return platform;
    }
}
