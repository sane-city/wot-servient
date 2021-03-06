/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package city.sane.wot.thing;

import city.sane.ObjectBuilder;
import city.sane.wot.thing.form.Form;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    protected Map<String, Map<String, Object>> uriVariables = new HashMap<>();

    public String getDescription() {
        return description;
    }

    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    public List<Form> getForms() {
        return forms;
    }

    public Map<String, Map<String, Object>> getUriVariables() {
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
        Map<String, Map<String, Object>> uriVariables = new HashMap<>();

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

        public T setUriVariables(Map<String, Map<String, Object>> uriVariables) {
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
