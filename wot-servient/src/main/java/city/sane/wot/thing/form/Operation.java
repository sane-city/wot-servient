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
