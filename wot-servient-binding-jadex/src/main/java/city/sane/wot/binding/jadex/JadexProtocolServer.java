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
import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.ProtocolServerException;
import city.sane.wot.thing.ExposedThing;
import com.typesafe.config.Config;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.search.ServiceQuery;
import jadex.bridge.service.types.cms.CreationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Allows exposing Things via Jadex Micro Agents.<br> Starts a Jadex Platform and a {@link
 * ThingsAgent}. This Agent is responsible for exposing Things. The Jadex Platform automatically
 * finds all other platforms and thus enables interaction with their Things. The Jadex Platform can
 * be configured via the configuration parameter "wot.servient.jadex.server".
 */
public class JadexProtocolServer implements ProtocolServer {
    private static final Logger log = LoggerFactory.getLogger(JadexProtocolServer.class);
    private final Map<String, ExposedThing> things;
    private final RefCountResource<IExternalAccess> platformProvider;
    private IExternalAccess platform;
    private ThingsService thingsService;

    public JadexProtocolServer(Config config) {
        this(SharedPlatformProvider.singleton(config), null, null, new HashMap<>());
    }

    JadexProtocolServer(RefCountResource<IExternalAccess> platformProvider,
                        IExternalAccess platform,
                        ThingsService thingsService,
                        Map<String, ExposedThing> things) {
        this.platformProvider = platformProvider;
        this.platform = platform;
        this.thingsService = thingsService;
        this.things = things;
    }

    @Override
    public CompletableFuture<Void> start(Servient servient) {
        log.info("JadexServer is starting Jadex Platform");

        if (platform == null) {
            return runAsync(() -> {
                try {
                    platform = platformProvider.retain();
                }
                catch (RefCountResourceException e) {
                    throw new CompletionException(e);
                }
            }).thenCompose(ignore -> {
                CreationInfo info = new CreationInfo()
                        .setFilenameClass(ThingsAgent.class)
                        .addArgument("things", things);
                return FutureConverters.fromJadex(platform.createComponent(info));
            }).thenCompose(agent -> FutureConverters.fromJadex(agent.searchService(new ServiceQuery<>(ThingsService.class)))).thenAccept(myThingsService -> this.thingsService = myThingsService);
        }
        else {
            return completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> stop() {
        log.info("JadexServer is stopping Jadex Platform '{}'", platform);

        if (platform != null) {
            return runAsync(() -> {
                try {
                    platform = null;
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

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        log.info("JadexServer exposes '{}'", thing.getId());

        if (platform == null) {
            return failedFuture(new ProtocolServerException("Unable to expose thing before JadexServer has been started"));
        }

        things.put(thing.getId(), thing);

        CompletableFuture<IExternalAccess> expose = FutureConverters.fromJadex(thingsService.expose(thing.getId()));

        return expose.thenApply(r -> null);
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        // if the server is not running, nothing needs to be done
        if (platform == null) {
            return completedFuture(null);
        }

        log.info("JadexServer stop exposing '{}'", thing.getId());

        if (things.remove(thing.getId()) == null) {
            return completedFuture(null);
        }

        if (thingsService != null) {
            return FutureConverters.fromJadex(thingsService.destroy(thing.getId()));
        }
        else {
            return completedFuture(null);
        }
    }
}
