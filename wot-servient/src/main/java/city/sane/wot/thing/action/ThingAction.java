package city.sane.wot.thing.action;

import city.sane.wot.thing.ThingInteraction;
import city.sane.wot.thing.schema.DataSchema;
import city.sane.wot.thing.schema.StringSchema;
import city.sane.wot.thing.schema.VariableDataSchema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;

/**
 * This class represents a read-only model of a thing action. The class {@link Builder} can be used
 * to build new thing action models. Used in combination with {@link city.sane.wot.thing.Thing}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThingAction<I, O> extends ThingInteraction<ThingAction<I, O>> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(as = VariableDataSchema.class)
    DataSchema<I> input;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(as = VariableDataSchema.class)
    DataSchema<O> output;

    ThingAction(DataSchema<I> input, DataSchema<O> output) {
        this.input = input;
        this.output = output;
    }

    public ThingAction() {
    }

    public DataSchema<I> getInput() {
        return input;
    }

    public DataSchema<O> getOutput() {
        return output;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), input, output);
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
        ThingAction<Object, Object> that = (ThingAction<Object, Object>) o;
        return Objects.equals(input, that.input) && Objects.equals(output, that.output);
    }

    @Override
    public String toString() {
        return "ThingAction{" +
                "input=" + input +
                ", output=" + output +
                ", description='" + description + '\'' +
                ", descriptions=" + descriptions +
                ", forms=" + forms +
                ", uriVariables=" + uriVariables +
                '}';
    }

    /**
     * Allows building new {@link ThingAction} objects.
     */
    public static class Builder extends AbstractBuilder<Builder> {
        private DataSchema input = new StringSchema();
        private DataSchema output = new StringSchema();

        public Builder setInput(DataSchema input) {
            this.input = input;
            return this;
        }

        public Builder setOutput(DataSchema output) {
            this.output = output;
            return this;
        }

        @Override
        public ThingAction<Object, Object> build() {
            ThingAction<Object, Object> action = new ThingAction<Object, Object>(input, output);
            applyInteractionParameters(action);
            return action;
        }
    }
}
