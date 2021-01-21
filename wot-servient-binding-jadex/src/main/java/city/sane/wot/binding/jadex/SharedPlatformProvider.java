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
import com.typesafe.config.Config;
import jadex.bridge.IExternalAccess;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a Singleton class, which is used by {@link JadexProtocolClient} and {@link
 * JadexProtocolServer} to share a single Jadex Platform.
 */
public class SharedPlatformProvider {
    private static final Map<Config, RefCountResource<IExternalAccess>> singletons = new HashMap<>();

    private SharedPlatformProvider() {
        // singleton class
    }

    public static synchronized RefCountResource<IExternalAccess> singleton(Config config) {
        return singletons.computeIfAbsent(
                config,
                myConfig -> new RefCountResource<>(
                        () -> {
                            JadexProtocolPlatformConfig jadexConfig = new JadexProtocolPlatformConfig(myConfig);
                            return jadexConfig.createPlatform();
                        },
                        platform -> FutureConverters.fromJadex(platform.killComponent()).join()
                )
        );
    }
}
