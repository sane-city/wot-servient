package city.sane.wot.thing.property;

import city.sane.wot.thing.ThingInteraction;
import city.sane.wot.thing.schema.DataSchema;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a read-only model of a thing property.
 * The class {@link Builder} can be used to build new thing property models.
 * Used in combination with {@link city.sane.wot.thing.Thing}
 */
public class ThingProperty extends ThingInteraction<ThingProperty> implements DataSchema {
    @JsonProperty("@type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String objectType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String type;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    protected boolean observable;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    protected boolean readOnly;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    protected boolean writeOnly;

    protected Map<String, Object> optionalProperties = new HashMap<>();

    public String getObjectType() {
        return objectType;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Class getClassType() {
        return Object.class;
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

    /**
     * Allows building new {@link ThingProperty} objects.
     */
    public static class Builder extends AbstractBuilder<Builder> {
        private String objectType;
        private String type;
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
        public ThingProperty build() {
            ThingProperty property = new ThingProperty();
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
