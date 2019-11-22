package city.sane.wot.thing.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Defines the type of data. Used e.g. to define which input and output values a {@link city.sane.wot.thing.action.ThingAction} has or of which type a
 * {@link city.sane.wot.thing.property.ThingProperty} is.<br>
 * See also: https://www.w3.org/TR/wot-thing-description/#sec-data-schema-vocabulary-definition
 *
 * @param <T>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public interface DataSchema<T> extends Serializable {
    String getType();

    @JsonIgnore
    Class<T> getClassType();
}
