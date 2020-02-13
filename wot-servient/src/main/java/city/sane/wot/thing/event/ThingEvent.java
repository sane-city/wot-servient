package city.sane.wot.thing.event;

import city.sane.wot.thing.ThingInteraction;
import city.sane.wot.thing.schema.DataSchema;
import city.sane.wot.thing.schema.StringSchema;
import city.sane.wot.thing.schema.VariableDataSchema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;

/**
 * This class represents a read-only model of a thing event. The class {@link Builder} can be used
 * to build new thing event models. Used in combination with {@link city.sane.wot.thing.Thing}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThingEvent<T> extends ThingInteraction<ThingEvent<T>> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(as = VariableDataSchema.class)
    DataSchema<T> data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String type;

    public String getType() {
        return type;
    }

    public DataSchema<T> getData() {
        return data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), data, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ThingEvent)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ThingEvent<Object> that = (ThingEvent<Object>) o;
        return Objects.equals(data, that.data) && Objects.equals(type, that.type);
    }

    @Override
    public String toString() {
        return "ThingEvent{" +
                "data=" + data +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", descriptions=" + descriptions +
                ", forms=" + forms +
                ", uriVariables=" + uriVariables +
                '}';
    }

    /**
     * Allows building new {@link ThingEvent} objects.
     */
    public static class Builder extends AbstractBuilder<Builder> {
        private DataSchema data = new StringSchema();
        private String type;

        public ThingEvent.Builder setData(DataSchema data) {
            this.data = data;
            return this;
        }

        public ThingEvent.Builder setType(String type) {
            this.type = type;
            return this;
        }

        @Override
        public ThingEvent<Object> build() {
            ThingEvent<Object> event = new ThingEvent<Object>();
            event.data = data;
            event.type = type;
            applyInteractionParameters(event);
            return event;
        }
    }
}
