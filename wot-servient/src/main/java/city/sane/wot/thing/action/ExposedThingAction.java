package city.sane.wot.thing.action;

import city.sane.wot.thing.ExposedThing;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Used in combination with {@link ExposedThing} and allows exposing of a {@link ThingAction}.
 */
public class ExposedThingAction extends ThingAction {
    private static final Logger log = LoggerFactory.getLogger(ExposedThingAction.class);

    private final String name;
    private final ExposedThing thing;

    @JsonIgnore
    private final ActionState state = new ActionState();

    public ExposedThingAction(String name, ThingAction action, ExposedThing thing) {
        this.name = name;
        this.description = action.getDescription();
        this.descriptions = action.getDescriptions();
        this.uriVariables = action.getUriVariables();
        this.input = action.getInput();
        this.output = action.getOutput();
        this.thing = thing;
    }

    public ActionState getState() {
        return state;
    }

    /**
     * Invokes the method and executes the handler defined in {@link #state}. <code>input</code> contains the request payload. <code>options</code> can contain
     * additional data (for example, the query parameters when using COAP/HTTP).
     *
     * @param input
     * @param options
     *
     * @return
     */
    public CompletableFuture<Object> invoke(Object input, Map<String, Object> options) {
        log.info("'{}' has Action state of '{}': {}", thing.getId(), name, getState());

        if (getState().getHandler() != null) {
            log.info("'{}' calls registered handler for Action '{}' with input '{}' and options '{}'", thing.getId(), name, input, options);
            return getState().getHandler().apply(input, options);
        }
        else {
            log.info("'{}' has no handler for Action '{}'", thing.getId(), name);
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Invokes the method and executes the handler defined in {@link #state}. <code>input</code> contains the request payload.
     *
     * @param input
     *
     * @return
     */
    public CompletableFuture<Object> invoke(Object input) {
        return invoke(input, Collections.emptyMap());
    }

    /**
     * Invokes the method and executes the handler defined in {@link #state}.
     *
     * @return
     */
    public CompletableFuture<Object> invoke() {
        return invoke(null);
    }
}
