package city.sane.wot.thing.action;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * This class represented the container for the handler of a {@link ThingAction}. The handler is
 * executed when the action is invoked.
 */
public class ActionState<I, O> {
    private BiFunction<I, Map<String, Object>, CompletableFuture<O>> handler;

    public ActionState() {
        this(null);
    }

    ActionState(BiFunction<I, Map<String, Object>, CompletableFuture<O>> handler) {
        this.handler = handler;
    }

    public BiFunction<I, Map<String, Object>, CompletableFuture<O>> getHandler() {
        return handler;
    }

    public void setHandler(BiFunction<I, Map<String, Object>, CompletableFuture<O>> handler) {
        this.handler = handler;
    }
}
