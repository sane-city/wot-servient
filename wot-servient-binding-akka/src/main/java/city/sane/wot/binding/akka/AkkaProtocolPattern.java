package city.sane.wot.binding.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.pattern.Patterns;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class AkkaProtocolPattern {
    public CompletionStage<Object> ask(ActorRef actor, Object message, Duration timeout) {
        return Patterns.ask(actor, message, timeout);
    }

    public CompletionStage<Object> ask(ActorSelection selection, Object message, Duration timeout) {
        return Patterns.ask(selection, message, timeout);
    }
}
