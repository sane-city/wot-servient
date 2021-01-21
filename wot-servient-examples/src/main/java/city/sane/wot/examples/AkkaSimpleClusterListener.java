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
package city.sane.wot.examples;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.Servient;
import city.sane.wot.ServientException;
import city.sane.wot.binding.akka.AkkaProtocolServer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * This example lists the members found in the Akka Cluster.
 */
class AkkaSimpleClusterListener extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private Cluster cluster = Cluster.get(getContext().getSystem());

    // subscribe to cluster changes
    @Override
    public void preStart() {
        // #subscribe
        cluster.subscribe(
                getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
        // #subscribe
    }

    // re-subscribe when restart
    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        MemberUp.class,
                        mUp -> log.debug("Member is Up: {}", mUp.member()))
                .match(
                        UnreachableMember.class,
                        mUnreachable -> log.debug("Member detected as unreachable: {}", mUnreachable.member()))
                .match(
                        MemberRemoved.class,
                        mRemoved -> log.debug("Member is Removed: {}", mRemoved.member()))
                .match(
                        MemberEvent.class,
                        message -> {
                            // ignore
                        })
                .build();
    }

    public static void main(String[] args) throws ServientException {
        Config config = ConfigFactory.load();

        Servient servient = new Servient(config);
        servient.start().join();

        AkkaProtocolServer server = servient.getServer(AkkaProtocolServer.class);
        ActorSystem system = server.getActorSystem();

        system.actorOf(Props.create(AkkaSimpleClusterListener.class, AkkaSimpleClusterListener::new));
    }
}