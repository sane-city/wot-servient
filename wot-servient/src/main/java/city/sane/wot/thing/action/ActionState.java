package city.sane.wot.thing.action;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * This class represented the container for the handler of a {@link ThingAction}. The handler is executed when the action is invoked.
 */
public class ActionState {
    private BiFunction<Object, Map<String, Object>, CompletableFuture<Object>> handler;

    public BiFunction<Object, Map<String, Object>, CompletableFuture<Object>> getHandler() {
        return handler;
    }

    public void setHandler(BiFunction<Object, Map<String, Object>, CompletableFuture<Object>> handler) {
        this.handler = handler;
    }
}
