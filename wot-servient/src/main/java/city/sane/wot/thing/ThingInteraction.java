package city.sane.wot.thing;

import city.sane.ObjectBuilder;
import city.sane.wot.thing.form.Form;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract representation of a Thing Interaction (inherited from {@link city.sane.wot.thing.action.ThingAction}, {@link city.sane.wot.thing.event.ThingEvent}
 * and {@link city.sane.wot.thing.property.ThingProperty})
 *
 * @param <T>
 */
public abstract class ThingInteraction<T> implements Serializable {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Map<String, String> descriptions;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected List<Form> forms = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected Map<String, Map> uriVariables = new HashMap<>();

    public String getDescription() {
        return description;
    }

    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    public List<Form> getForms() {
        return forms;
    }

    public Map<String, Map> getUriVariables() {
        return uriVariables;
    }

    public T setForms(List<Form> forms) {
        this.forms = forms;
        return (T) this;
    }

    public T addForm(Form form) {
        forms.add(form);
        return (T) this;
    }

    public abstract static class Builder<T extends ObjectBuilder> implements ObjectBuilder {
        protected String description;
        protected Map<String, String> descriptions;
        protected List<Form> forms = new ArrayList<>();
        protected Map<String, Map> uriVariables = new HashMap<>();

        public T setDescription(String description) {
            this.description = description;
            return (T) this;
        }

        public T setDescriptions(Map<String, String> descriptions) {
            this.descriptions = descriptions;
            return (T) this;
        }

        public T setDescription(Map<String, String> descriptions) {
            this.descriptions = descriptions;
            return (T) this;
        }

        public T setForms(List<Form> forms) {
            this.forms = forms;
            return (T) this;
        }

        public T addForm(Form form) {
            forms.add(form);
            return (T) this;
        }

        public T setUriVariables(Map<String, Map> uriVariables) {
            this.uriVariables = uriVariables;
            return (T) this;
        }

        protected void applyInteractionParameters(ThingInteraction interaction) {
            interaction.description = description;
            interaction.descriptions = descriptions;
            interaction.forms = forms;
            interaction.uriVariables = uriVariables;
        }
    }
}
