package city.sane.wot.thing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a JSON thing type.
 */
@JsonDeserialize(using = TypeDeserializer.class)
@JsonSerialize(using = TypeSerializer.class)
public class Type {
    private final Set<String> types = new HashSet<>();

    public Type() {
    }

    public Type(String type) {
        addType(type);
    }

    public Type addType(String type) {
        types.add(type);
        return this;
    }

    public Set<String> getTypes() {
        return types;
    }

    @Override
    public int hashCode() {
        return Objects.hash(types);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Type type = (Type) o;
        return Objects.equals(types, type.types);
    }

    @Override
    public String toString() {
        return "Type{" +
                "types=" + types +
                '}';
    }
}
