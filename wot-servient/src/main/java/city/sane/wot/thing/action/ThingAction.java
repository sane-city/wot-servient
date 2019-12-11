package city.sane.wot.thing.action;

import city.sane.wot.thing.ThingInteraction;
import city.sane.wot.thing.schema.DataSchema;
import city.sane.wot.thing.schema.VariableDataSchema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;

/**
 * This class represents a read-only model of a thing action.
 * The class {@link Builder} can be used to build new thing action models.
 * Used in combination with {@link city.sane.wot.thing.Thing}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThingAction extends ThingInteraction<ThingAction> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(as = VariableDataSchema.class)
    DataSchema input;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(as = VariableDataSchema.class)
    DataSchema output;

    public DataSchema getInput() {
        return input;
    }

    public DataSchema getOutput() {
        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ThingAction)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ThingAction that = (ThingAction) o;
        return super.equals(that) &&
                Objects.equals(input, that.input) &&
                Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), input, output);
    }

    /**
     * Allows building new {@link ThingAction} objects.
     */
    public static class Builder extends AbstractBuilder<Builder> {
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
