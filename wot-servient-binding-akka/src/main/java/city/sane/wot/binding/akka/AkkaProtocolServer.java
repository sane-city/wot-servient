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
package city.sane.wot.binding.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import city.sane.RefCountResource;
import city.sane.RefCountResourceException;
import city.sane.Triple;
import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.ProtocolServerException;
import city.sane.wot.binding.akka.actor.ThingsActor;
import city.sane.wot.thing.ExposedThing;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Allows exposing Things via Akka Actors.<br> Starts an Actor System with an {@link ThingsActor}
 * actuator. This Actuator is responsible for exposing Things. The Actor System is intended for use
 * in an <a href="https://doc.akka.io/docs/akka/current/index-cluster.html">Akka Cluster</a> to
 * discover and interact with other Actuator Systems.<br> The Actor System can be configured via the
 * configuration parameter "wot.servient.akka" (see https://doc.akka.io/docs/akka/current/general/configuration.html).
 */
public class AkkaProtocolServer implements ProtocolServer {
    private static final Logger log = LoggerFactory.getLogger(AkkaProtocolServer.class);
    private final RefCountResource<Triple<ActorSystem, Map<String, ExposedThing>, ActorRef>> actorSystemProvider;
    private final AkkaProtocolPattern pattern;
    private Triple<ActorSystem, Map<String, ExposedThing>, ActorRef> triple;

    public AkkaProtocolServer(Config config) {
        this(
                SharedActorSystemProvider.singleton(config),
                new AkkaProtocolPattern()
        );
    }

    protected AkkaProtocolServer(RefCountResource<Triple<ActorSystem, Map<String, ExposedThing>, ActorRef>> actorSystemProvider,
                                 AkkaProtocolPattern pattern) {
        this(actorSystemProvider, pattern, null);
    }

    protected AkkaProtocolServer(RefCountResource<Triple<ActorSystem, Map<String, ExposedThing>, ActorRef>> actorSystemProvider,
                                 AkkaProtocolPattern pattern,
                                 Triple<ActorSystem, Map<String, ExposedThing>, ActorRef> triple) {
        this.actorSystemProvider = actorSystemProvider;
        this.pattern = pattern;
        this.triple = triple;
    }

    @Override
    public String toString() {
        return "AkkaServer";
    }

    @Override
    public CompletableFuture<Void> start(Servient servient) {
        log.info("Start AkkaServer");

        if (triple == null) {
            return runAsync(() -> {
                try {
                    triple = actorSystemProvider.retain();
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
    public CompletableFuture<Void> stop() {
        log.info("Stop AkkaServer");

        if (triple != null) {
            return runAsync(() -> {
                try {
                    triple = null;
                    actorSystemProvider.release();
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
        log.info("AkkaServer exposes '{}'", thing.getId());

        if (triple == null) {
            return failedFuture(new ProtocolServerException("Unable to expose thing before AkkaServer has been started"));
        }

        triple.second().put(thing.getId(), thing);

        Duration timeout = Duration.ofSeconds(10);
        return pattern.ask(triple.third(), new ThingsActor.Expose(thing.getId()), timeout)
                .thenRun(() -> log.debug("AkkaServer has '{}' exposed", thing.getId()))
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        // if the server is not running, nothing needs to be done
        if (triple == null) {
            return completedFuture(null);
        }

        log.info("AkkaServer stop exposing '{}'", thing.getId());

        if (triple.second().remove(thing.getId()) == null) {
            return completedFuture(null);
        }

        Duration timeout = Duration.ofSeconds(10);
        return pattern.ask(triple.third(), new ThingsActor.Destroy(thing.getId()), timeout)
                .thenRun(() -> log.debug("AkkaServer does not expose more '{}'", thing.getId()))
                .toCompletableFuture();
    }

    @Override
    public URI getDirectoryUrl() {
        try {
            String endpoint = triple.third().path().toStringWithAddress(triple.first().provider().getDefaultAddress());
            return new URI(endpoint + "#thing-directory");
        }
        catch (URISyntaxException e) {
            log.warn("Unable to create directory url", e);
            return null;
        }
    }

    @Override
    public URI getThingUrl(String id) {
        try {
            String endpoint = triple.third().path().child(id).toStringWithAddress(triple.first().provider().getDefaultAddress());
            return new URI(endpoint + "#thing");
        }
        catch (URISyntaxException e) {
            log.warn("Unable to create thing url", e);
            return null;
        }
    }

    public ActorSystem getActorSystem() {
        return triple.first();
    }
}