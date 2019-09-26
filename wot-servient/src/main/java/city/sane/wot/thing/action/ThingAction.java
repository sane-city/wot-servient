package city.sane.wot.thing.action;

import city.sane.wot.thing.ThingInteraction;
import city.sane.wot.thing.schema.DataSchema;
import city.sane.wot.thing.schema.VariableDataSchema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * This class represents a read-only model of a thing action.
 * The class {@link Builder} can be used to build new thing action models.
 * Used in combination with {@link city.sane.wot.thing.Thing}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThingAction extends ThingInteraction<ThingAction> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(as = VariableDataSchema.class)
    protected DataSchema input;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(as = VariableDataSchema.class)
    protected DataSchema output;

    public DataSchema getInput() {
        return input;
    }

    public DataSchema getOutput() {
        return output;
    }

    /**
     * Allows building new {@link ThingAction} objects.
     */
    public static class Builder extends ThingInteraction.Builder<Builder> {
        private DataSchema input;
        private DataSchema output;

        public Builder setInput(DataSchema input) {
            this.input = input;
            return this;
        }

        public Builder setOutput(DataSchema output) {
            this.output = output;
            return this;
        }

        @Override
        public ThingAction build() {
            ThingAction action = new ThingAction();
            action.input = input;
            action.output = output;
            applyInteractionParameters(action);
            return action;
        }
    }
}
