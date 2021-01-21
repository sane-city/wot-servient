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
package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.binding.akka.Message;
import city.sane.wot.content.Content;
import io.reactivex.rxjava3.core.Observer;

import static java.util.Objects.requireNonNull;

/**
 * This actor is temporarily created for a obersavtion of an event/a property.
 */
public class ObserveActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Observer<Content> observer;
    private final ActorSelection selection;
    private final Message subscribeMessage;

    public ObserveActor(Observer<Content> observer,
                        ActorSelection selection,
                        Message subscribeMessage) {
        this.observer = observer;
        this.selection = selection;
        this.subscribeMessage = subscribeMessage;
    }

    @Override
    public void preStart() {
        log.debug("Started");

        log.debug("Send Subscribe message to {}", selection);
        selection.tell(subscribeMessage, getSelf());
    }

    @Override
    public void postStop() {
        log.debug("Stop");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Message.SubscriptionNext.class, m -> observer.onNext(m.content))
                .match(Message.SubscriptionError.class, m -> {
                    observer.onError(m.e);
                    getContext().stop(getSelf());
                })
                .match(Message.SubscriptionComplete.class, m -> {
                    observer.onComplete();
                    getContext().stop(getSelf());
                })
                .build();
    }

    public static Props props(Observer<Content> observer,
                              ActorSelection selection,
                              Message subscribeMessage) {
        return Props.create(ObserveActor.class, () -> new ObserveActor(requireNonNull(observer), requireNonNull(selection), requireNonNull(subscribeMessage)));
    }
}
