package city.sane.wot.thing.property;

import city.sane.wot.thing.ThingInteraction;
import city.sane.wot.thing.schema.DataSchema;
import city.sane.wot.thing.schema.VariableDataSchema;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class represents a read-only model of a thing property. The class {@link Builder} can be
 * used to build new thing property models. Used in combination with {@link
 * city.sane.wot.thing.Thing}
 */
public class ThingProperty<T> extends ThingInteraction<ThingProperty<T>> implements DataSchema<T> {
    @JsonProperty("@type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String objectType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String type;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    boolean observable;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    boolean readOnly;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    boolean writeOnly;
    Map<String, Object> optionalProperties = new HashMap<>();

    public String getObjectType() {
        return objectType;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Class<T> getClassType() {
        return new VariableDataSchema.Builder().setType(type).build().getClassType();
    }

    public boolean isObservable() {
        return observable;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isWriteOnly() {
        return writeOnly;
    }

    @JsonAnyGetter
    public Map<String, Object> getOptionalProperties() {
        return optionalProperties;
    }

    public Object getOptional(String name) {
        return optionalProperties.get(name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), objectType, type, observable, readOnly, writeOnly, optionalProperties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ThingProperty)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ThingProperty<Object> that = (ThingProperty<Object>) o;
        return observable == that.observable &&
                readOnly == that.readOnly &&
                writeOnly == that.writeOnly &&
                Objects.equals(objectType, that.objectType) &&
                Objects.equals(type, that.type) &&
                Objects.equals(optionalProperties, that.optionalProperties);
    }

    @Override
    public String toString() {
        return "ThingProperty{" +
                "objectType='" + objectType + '\'' +
                ", type='" + type + '\'' +
                ", observable=" + observable +
                ", readOnly=" + readOnly +
                ", writeOnly=" + writeOnly +
                ", optionalProperties=" + optionalProperties +
                ", description='" + description + '\'' +
                ", descriptions=" + descriptions +
                ", forms=" + forms +
                ", uriVariables=" + uriVariables +
                '}';
    }

    /**
     * Allows building new {@link ThingProperty} objects.
     */
    public static class Builder extends AbstractBuilder<Builder> {
        private String objectType;
        private String type = "string";
        private boolean observable;
        private boolean readOnly;
        private boolean writeOnly;
        private Map<String, Object> optionalProperties = new HashMap<>();

        public Builder setObjectType(String objectType) {
            this.objectType = objectType;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setObservable(boolean observable) {
            this.observable = observable;
            return this;
        }

        public Builder setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public Builder setWriteOnly(boolean writeOnly) {
            this.writeOnly = writeOnly;
            return this;
        }

        public Builder setOptionalProperties(Map<String, Object> optionalProperties) {
            this.optionalProperties = optionalProperties;
            return this;
        }

        @JsonAnySetter
        public Builder setOptional(String name, String value) {
            optionalProperties.put(name, value);
            return this;
        }

        @Override
        public ThingProperty<Object> build() {
            ThingProperty<Object> property = new ThingProperty<>();
            property.objectType = objectType;
            property.type = type;
            property.observable = observable;
            property.readOnly = readOnly;
            property.writeOnly = writeOnly;
            property.optionalProperties = optionalProperties;
            applyInteractionParameters(property);
            return property;
        }
    }
}
