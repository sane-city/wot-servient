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
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import city.sane.Pair;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.ProtocolClientNotImplementedException;
import city.sane.wot.binding.akka.Message.ContentMessage;
import city.sane.wot.binding.akka.actor.DiscoverActor;
import city.sane.wot.binding.akka.actor.ObserveActor;
import city.sane.wot.binding.akka.actor.ThingActor.GetThingDescription;
import city.sane.wot.binding.akka.actor.ThingActor.InvokeAction;
import city.sane.wot.binding.akka.actor.ThingActor.ReadAllProperties;
import city.sane.wot.binding.akka.actor.ThingActor.ReadProperty;
import city.sane.wot.binding.akka.actor.ThingActor.SubscribeEvent;
import city.sane.wot.binding.akka.actor.ThingActor.SubscribeProperty;
import city.sane.wot.binding.akka.actor.ThingActor.WriteProperty;
import city.sane.wot.binding.akka.actor.ThingsActor.GetThings;
import city.sane.wot.content.Content;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.form.Form;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.failedFuture;

/**
 * Allows consuming Things via Akka Actors.<br> The Actor System created by {@link
 * AkkaProtocolClientFactory} is used for this purpose and thus enables interaction with exposed
 * Things on other actuator systems.
 */
public class AkkaProtocolClient implements ProtocolClient {
    private static final Logger log = LoggerFactory.getLogger(AkkaProtocolClient.class);
    protected final ActorSystem system;
    protected final Duration askTimeout;
    protected final AkkaProtocolPattern pattern;
    private final Duration discoverTimeout;

    public AkkaProtocolClient(ActorSystem system,
                              Duration askTimeout,
                              Duration discoverTimeout) {
        this(system, askTimeout, discoverTimeout, new AkkaProtocolPattern());
    }

    AkkaProtocolClient(ActorSystem system,
                       Duration askTimeout,
                       Duration discoverTimeout,
                       AkkaProtocolPattern pattern) {
        this.system = system;
        this.askTimeout = askTimeout;
        this.discoverTimeout = discoverTimeout;
        this.pattern = pattern;
    }

    @SuppressWarnings("squid:S1192")
    @Override
    public CompletableFuture<Content> readResource(Form form) {
        return request(form, resourceIdentifier -> {
            if (resourceIdentifier.length == 2 && resourceIdentifier[0].equals("properties")) {
                return new ReadProperty(resourceIdentifier[1]);
            }
            else if (resourceIdentifier.length == 2 && resourceIdentifier[0].equals("all") && resourceIdentifier[1].equals("properties")) {
                return new ReadAllProperties();
            }
            else if (resourceIdentifier.length == 1 && resourceIdentifier[0].equals("thing")) {
                return new GetThingDescription();
            }
            else if (resourceIdentifier.length == 1 && resourceIdentifier[0].equals("thing-directory")) {
                return new GetThings();
            }
            else {
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Content> writeResource(Form form, Content content) {
        return request(form, resourceIdentifier -> {
            if (resourceIdentifier.length == 2 && resourceIdentifier[0].equals("properties")) {
                return new WriteProperty(resourceIdentifier[1], content);
            }
            else {
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Content> invokeResource(Form form, Content content) {
        return request(form, resourceIdentifier -> {
            if (resourceIdentifier.length == 2 && resourceIdentifier[0].equals("actions")) {
                return new InvokeAction(resourceIdentifier[1], content);
            }
            else {
                return null;
            }
        });
    }

    @Override
    public Observable<Content> observeResource(Form form) {
        return observe(form, resourceIdentifier -> {
            if (resourceIdentifier.length == 2 && resourceIdentifier[0].equals("properties")) {
                return new SubscribeProperty(resourceIdentifier[1]);
            }
            else if (resourceIdentifier.length == 2 && resourceIdentifier[0].equals("events")) {
                return new SubscribeEvent(resourceIdentifier[1]);
            }
            else {
                return null;
            }
        });
    }

    @Override
    public Observable<Thing> discover(ThingFilter filter) throws ProtocolClientNotImplementedException {
        if (system.settings().config() != null && system.settings().config().getStringList("wot.servient.akka.extensions").contains("akka.cluster.pubsub.DistributedPubSub")) {
            return Observable.using(
                    () -> {
                        log.debug("Create temporary actor to discover things matching filter: {}", filter);
                        PublishSubject<Thing> subject = PublishSubject.create();
                        ActorRef actorRef = system.actorOf(DiscoverActor.props(subject, filter, discoverTimeout));
                        return new Pair<>(subject, actorRef);
                    },
                    Pair::first,
                    pair -> {
                        log.debug("No more observers. Stop temporary actor for thing discovery with filter: {}", filter);
                        system.stop(pair.second());
                    }
            );
        }
        else {
            log.warn("DistributedPubSub extension missing. ANY Discovery is not be supported.");
            throw new ProtocolClientNotImplementedException(getClass(), "discover");
        }
    }

    private Observable<Content> observe(Form form,
                                        Function<String[], Message> messageProvider) {
        try {
            Pair<String, String[]> pair = getActorPathAndResourceIdentifier(form);
            String actorPath = pair.first();
            String[] resourceIdentifier = pair.second();

            Message message = messageProvider.apply(resourceIdentifier);

            if (message != null) {
                log.debug("AkkaClient sending '{}' to {}", message, actorPath);
                ActorSelection selection = system.actorSelection(actorPath);
                return Observable.using(
                        () -> {
                            log.debug("Create temporary actor to observe resource: {}", actorPath);
                            PublishSubject<Content> subject = PublishSubject.create();
                            ActorRef actorRef = system.actorOf(ObserveActor.props(subject, selection, message));
                            return new Pair<>(subject, actorRef);
                        },
                        Pair::first,
                        myPair -> {
                            log.debug("No more observers. Stop temporary actor from resource observation: {}", actorPath);
                            system.stop(myPair.second());
                        }
                );
            }
            else {
                return Observable.error(new ProtocolClientException("Unknown resource identifier: " + String.join("/", resourceIdentifier)));
            }
        }
        catch (ProtocolClientException e) {
            return Observable.error(e);
        }
    }

    private CompletableFuture<Content> request(Form form,
                                               Function<String[], Message> messageProvider) {
        try {
            Pair<String, String[]> pair = getActorPathAndResourceIdentifier(form);
            String actorPath = pair.first();
            String[] resourceIdentifier = pair.second();

            Message message = messageProvider.apply(resourceIdentifier);

            if (message != null) {
                log.debug("AkkaClient sending '{}' to {}", message, actorPath);
                ActorSelection selection = system.actorSelection(actorPath);
                return pattern.ask(selection, message, askTimeout)
                        .thenApply(m -> ((ContentMessage) m).content)
                        .toCompletableFuture();
            }
            else {
                return failedFuture(new ProtocolClientException("Unknown resource identifier: " + String.join("/", resourceIdentifier)));
            }
        }
        catch (ProtocolClientException e) {
            return failedFuture(e);
        }
    }

    private Pair<String, String[]> getActorPathAndResourceIdentifier(Form form) throws ProtocolClientException {
        String[] parts = form.getHref().split("#", 2);
        if (parts.length == 2) {
            return new Pair<>(parts[0], parts[1].split("/"));
        }
        else {
            throw new ProtocolClientException("Unable to read resource identifier from href " + form.getHref());
        }
    }
}
