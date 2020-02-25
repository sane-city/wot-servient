package city.sane.wot.thing;

import city.sane.ObjectBuilder;
import city.sane.wot.thing.form.Form;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

/**
 * Abstract representation of a Thing Interaction (inherited from {@link
 * city.sane.wot.thing.action.ThingAction}, {@link city.sane.wot.thing.event.ThingEvent} and {@link
 * city.sane.wot.thing.property.ThingProperty})
 *
 * @param <T>
 */
public abstract class ThingInteraction<T> {
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

    @Override
    public int hashCode() {
        return Objects.hash(description, descriptions, forms, uriVariables);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ThingInteraction)) {
            return false;
        }
        ThingInteraction<?> that = (ThingInteraction<?>) o;
        return Objects.equals(description, that.description) &&
                Objects.equals(descriptions, that.descriptions) &&
                Objects.equals(forms, that.forms) &&
                Objects.equals(uriVariables, that.uriVariables);
    }

    public abstract static class AbstractBuilder<T extends ObjectBuilder> implements ObjectBuilder {
        String description;
        Map<String, String> descriptions;
        List<Form> forms = new ArrayList<>();
        Map<String, Map> uriVariables = new HashMap<>();

        public T setDescription(String description) {
            this.description = description;
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
