package city.sane.wot.thing.action;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.schema.DataSchema;
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
    private final ActionState state;

    ExposedThingAction(String name,
                       ExposedThing thing,
                       ActionState state,
                       String description,
                       Map<String, String> descriptions,
                       Map<String, Map> uriVariables,
                       DataSchema input,
                       DataSchema output) {
        this.name = name;
        this.thing = thing;
        this.state = state;
        this.description = description;
        this.descriptions = descriptions;
        this.uriVariables = uriVariables;
        this.input = input;
        this.output = output;
    }

    public ExposedThingAction(String name, ThingAction action, ExposedThing thing) {
        this(name, thing, new ActionState(), action.getDescription(), action.getDescriptions(), action.getUriVariables(), action.getInput(), action.getOutput());
    }

    public ActionState getState() {
        return state;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
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
        log.debug("'{}' has Action state of '{}': {}", thing.getId(), name, getState());

        if (getState().getHandler() != null) {
            log.debug("'{}' calls registered handler for Action '{}' with input '{}' and options '{}'", thing.getId(), name, input, options);
            CompletableFuture<Object> output = getState().getHandler().apply(input, options);
            if (output == null) {
                log.warn("'{}': Called registered handler for Action '{}' returned null. This can cause problems. Give Future with null result back.", thing.getId(), name);
                output = CompletableFuture.completedFuture(null);
            }
            return output;
        }
        else {
            log.debug("'{}' has no handler for Action '{}'", thing.getId(), name);
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
