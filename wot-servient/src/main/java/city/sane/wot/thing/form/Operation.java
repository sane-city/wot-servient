package city.sane.wot.thing.form;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the operation (e.g. read or write) on which a Thing Interaction is based.
 */
public enum Operation {
    // properties
    READ_PROPERTY("readproperty"),
    WRITE_PROPERTY("writeproperty"),
    OBSERVE_PROPERTY("observeproperty"),
    UNOBSERVE_PROPERTY("unobserveproperty"),
    READ_ALL_PROPERTIES("readallproperty"),
    READ_MULTIPLE_PROPERTIES("readmultipleproperty"),

    // events
    SUBSCRIBE_EVENT("subscribeevent"),
    UNSUBSCRIBE_EVENT("unsubscribeevent"),

    // actions
    INVOKE_ACTION("invokeaction");

    private static final Map<String, Operation> LOOKUP = new HashMap<>();

    static {
        for (Operation env : Operation.values()) {
            LOOKUP.put(env.toJsonValue(), env);
        }
    }

    private final String tdValue;

    Operation(String tdValue) {
        this.tdValue = tdValue;
    }

    @JsonValue
    private String toJsonValue() {
        return tdValue;
    }

    @JsonCreator
    public static Operation fromJsonValue(String jsonValue) {
        return LOOKUP.get(jsonValue);
    }
}
