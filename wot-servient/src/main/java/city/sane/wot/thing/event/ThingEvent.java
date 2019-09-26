package city.sane.wot.thing.event;

import city.sane.wot.thing.ThingInteraction;
import city.sane.wot.thing.schema.DataSchema;
import city.sane.wot.thing.schema.VariableDataSchema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * This class represents a read-only model of a thing event.
 * The class {@link Builder} can be used to build new thing event models.
 * Used in combination with {@link city.sane.wot.thing.Thing}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThingEvent extends ThingInteraction<ThingEvent> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(as = VariableDataSchema.class)
    protected DataSchema data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String type;

    public String getType() {
        return type;
    }

    public DataSchema getData() {
        return data;
    }

    /**
     * Allows building new {@link ThingEvent} objects.
     */
    public static class Builder extends ThingInteraction.Builder<Builder> {
        private DataSchema data;
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
        public ThingEvent build() {
            ThingEvent event = new ThingEvent();
            event.data = data;
            event.type = type;
            applyInteractionParameters(event);
            return event;
        }
    }
}
