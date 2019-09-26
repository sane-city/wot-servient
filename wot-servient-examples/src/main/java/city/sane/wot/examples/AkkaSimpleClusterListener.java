/*
 * Copyright (C) 2018-2019 Lightbend Inc. <https://www.lightbend.com>
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
import city.sane.wot.binding.akka.AkkaProtocolServer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * This example lists the members found in the Akka Cluster.
 */
public class AkkaSimpleClusterListener extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    Cluster cluster = Cluster.get(getContext().getSystem());

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
                        mUp -> {
                            log.info("Member is Up: {}", mUp.member());
                        })
                .match(
                        UnreachableMember.class,
                        mUnreachable -> {
                            log.info("Member detected as unreachable: {}", mUnreachable.member());
                        })
                .match(
                        MemberRemoved.class,
                        mRemoved -> {
                            log.info("Member is Removed: {}", mRemoved.member());
                        })
                .match(
                        MemberEvent.class,
                        message -> {
                            // ignore
                        })
                .build();
    }

    public static void main(String[] args) {
        Config config = ConfigFactory.load();

        Servient servient = new Servient(config);
        servient.start().join();

        AkkaProtocolServer server = servient.getServer(AkkaProtocolServer.class);
        ActorSystem system = server.getActorSystem();

        system.actorOf(Props.create(AkkaSimpleClusterListener.class, AkkaSimpleClusterListener::new));
    }
}