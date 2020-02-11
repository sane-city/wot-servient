package city.sane.wot.thing;

import city.sane.ObjectBuilder;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.security.SecurityScheme;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * This class represents a read-only model of a thing. The class {@link Builder} can be used to
 * build new thing models.
 *
 * @param <P>
 * @param <A>
 * @param <E>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Thing<P extends ThingProperty, A extends ThingAction, E extends ThingEvent> implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(Thing.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    @JsonProperty("@type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String objectType;
    @JsonProperty("@context")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Context objectContext;
    String id;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String title;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Map<String, String> titles;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String description;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Map<String, String> descriptions;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Map<String, P> properties = new HashMap<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Map<String, A> actions = new HashMap<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Map<String, E> events = new HashMap<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<Form> forms = new ArrayList<>();
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<String> security = new ArrayList<>();
    @JsonProperty("securityDefinitions")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Map<String, SecurityScheme> securityDefinitions = new HashMap<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String base;

    Thing(String objectType,
          Context objectContext,
          String id,
          String title,
          Map<String, String> titles,
          String description,
          Map<String, String> descriptions,
          Map<String, P> properties,
          Map<String, A> actions,
          Map<String, E> events,
          List<Form> forms,
          List<String> security,
          Map<String, SecurityScheme> securityDefinitions,
          String base) {
        this.objectType = objectType;
        this.objectContext = objectContext;
        this.id = id;
        this.title = title;
        this.titles = titles;
        this.description = description;
        this.descriptions = descriptions;
        this.properties = properties;
        this.actions = actions;
        this.events = events;
        this.forms = forms;
        this.security = security;
        this.securityDefinitions = securityDefinitions;
        this.base = base;
    }

    public Thing() {

    }

    public String getObjectType() {
        return objectType;
    }

    public Context getObjectContext() {
        return objectContext;
    }

    public String getTitle() {
        return title;
    }

    public Map<String, String> getTitles() {
        return titles;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    public List<Form> getForms() {
        return forms;
    }

    public P getProperty(String name) {
        return properties.get(name);
    }

    public Map<String, A> getActions() {
        return actions;
    }

    public A getAction(String name) {
        return actions.get(name);
    }

    public Map<String, E> getEvents() {
        return events;
    }

    public E getEvent(String name) {
        return events.get(name);
    }

    List<String> getSecurity() {
        return security;
    }

    Map<String, SecurityScheme> getSecurityDefinitions() {
        return securityDefinitions;
    }

    public String getBase() {
        return base;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Thing)) {
            return false;
        }
        return getId().equals(((Thing) obj).getId());
    }

    @Override
    public String toString() {
        return "Thing{" +
                "objectType='" + objectType + '\'' +
                ", objectContext=" + objectContext +
                ", id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", titles=" + titles +
                ", description='" + description + '\'' +
                ", descriptions=" + descriptions +
                ", properties=" + properties +
                ", actions=" + actions +
                ", events=" + events +
                ", forms=" + forms +
                ", security=" + security +
                ", securityDefinitions=" + securityDefinitions +
                ", base='" + base + '\'' +
                '}';
    }

    public String toJson() {
        return toJson(false);
    }

    public String toJson(boolean prettyPrint) {
        try {
            if (prettyPrint) {
                return JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            }
            else {
                return JSON_MAPPER.writeValueAsString(this);
            }
        }
        catch (JsonProcessingException e) {
            log.warn("Unable to create json", e);
            return null;
        }
    }

    /**
     * Returns a map of the properties and their keys that have the non-expanded JSON-LD type
     * <code>objectType</code>.
     *
     * @param objectType
     * @return
     */
    public Map<String, P> getPropertiesByObjectType(String objectType) {
        return getPropertiesByExpandedObjectType(getExpandedObjectType(objectType));
    }

    /**
     * Returns a map of the properties and their keys that have the expanded JSON-LD type
     * <code>objectType</code>.
     *
     * @param objectType
     * @return
     */
    public Map<String, P> getPropertiesByExpandedObjectType(String objectType) {
        HashMap<String, P> results = new HashMap<>();
        getProperties().forEach((key, property) -> {
            if (getExpandedObjectType(property.getObjectType()) != null && getExpandedObjectType(property.getObjectType()).equals(objectType)) {
                results.put(key, property);
            }
        });
        return results;
    }

    public String getExpandedObjectType(String objectType) {
        if (objectType == null || objectContext == null) {
            return null;
        }

        String[] parts = objectType.split(":", 2);
        String prefix;
        String suffix;
        if (parts.length == 2) {
            prefix = parts[0];
            suffix = parts[1];
        }
        else {
            prefix = null;
            suffix = objectType;
        }

        String url = objectContext.getUrl(prefix);

        if (url != null) {
            return url + suffix;
        }
        else {
            return objectType;
        }
    }

    public Map<String, P> getProperties() {
        return properties;
    }

    public static String randomId() {
        return "urn:uuid:" + UUID.randomUUID();
    }

    public static Thing fromJson(String json) {
        try {
            return JSON_MAPPER.readValue(json, Thing.class);
        }
        catch (IOException e) {
            log.warn("Unable to read json", e);
            return null;
        }
    }

    public static Thing fromJson(File json) {
        try {
            return JSON_MAPPER.readValue(json, Thing.class);
        }
        catch (IOException e) {
            log.warn("Unable to read json", e);
            return null;
        }
    }

    public static Thing fromMap(Map<String, Map> map) {
        return JSON_MAPPER.convertValue(map, Thing.class);
    }

    /**
     * Allows building new {@link Thing} objects.
     */
    public static class Builder implements ObjectBuilder<Thing> {
        private String objectType;
        private Context objectContext;
        private String id;
        private String title;
        private Map<String, String> titles;
        private String description;
        private Map<String, String> descriptions;
        private Map<String, ThingProperty> properties = new HashMap<>();
        private Map<String, ThingAction> actions = new HashMap<>();
        private Map<String, ThingEvent> events = new HashMap<>();
        private List<Form> forms = new ArrayList<>();
        private List<String> security = new ArrayList<>();
        private Map<String, SecurityScheme> securityDefinitions = new HashMap<>();
        private String base;

        public Builder setObjectType(String objectType) {
            this.objectType = objectType;
            return this;
        }

        public Builder setObjectContext(Context objectContext) {
            this.objectContext = objectContext;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTitles(Map<String, String> titles) {
            this.titles = titles;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setDescriptions(Map<String, String> descriptions) {
            this.descriptions = descriptions;
            return this;
        }

        public Builder addProperty(String name, ThingProperty property) {
            properties.put(name, property);
            return this;
        }

        public Builder addAction(String name, ThingAction action) {
            actions.put(name, action);
            return this;
        }

        public Builder addEvent(String name, ThingEvent event) {
            events.put(name, event);
            return this;
        }

        public Builder addForm(Form form) {
            forms.add(form);
            return this;
        }

        public Builder setForms(List<Form> forms) {
            this.forms = forms;
            return this;
        }

        public Builder setProperties(Map<String, ThingProperty> properties) {
            this.properties = properties;
            return this;
        }

        public Builder setActions(Map<String, ThingAction> actions) {
            this.actions = actions;
            return this;
        }

        public Builder setEvents(Map<String, ThingEvent> events) {
            this.events = events;
            return this;
        }

        public Builder setSecurity(List<String> security) {
            this.security = security;
            return this;
        }

        public Builder setSecurityDefinitions(Map<String, SecurityScheme> securityDefinitions) {
            this.securityDefinitions = securityDefinitions;
            return this;
        }

        public Builder setBase(String base) {
            this.base = base;
            return this;
        }

        @Override
        public Thing build() {
            Thing thing = new Thing(objectType, objectContext, id, title, titles, description, descriptions, properties, actions, events, forms, security, securityDefinitions, base);
            return thing;
        }
    }
}
