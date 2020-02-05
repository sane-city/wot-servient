package city.sane.wot.thing.form;

import city.sane.ObjectBuilder;
import city.sane.wot.thing.ConsumedThing;
import com.fasterxml.jackson.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * A form contains all the information from an endpoint for communication.<br>
 * See also: https://www.w3.org/TR/wot-thing-description/#form
 */
public class Form implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(Form.class);

    private String href;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Operation> op;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String subprotocol;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String contentType;

    private Map<String, Object> optionalProperties = new HashMap<>();

    public String getHref() {
        return href;
    }

    @Override
    public String toString() {
        return "Form{" +
                "href='" + href + '\'' +
                ", op=" + op +
                ", subprotocol='" + subprotocol + '\'' +
                ", contentType='" + contentType + '\'' +
                ", optionalProperties=" + optionalProperties +
                '}';
    }

    @JsonIgnore
    public String getHrefScheme() {
        try {
            // remove uri variables first
            String sanitizedHref = href;
            int index = href.indexOf('{');
            if (index != -1) {
                sanitizedHref = sanitizedHref.substring(0, index);
            }
            return new URI(sanitizedHref).getScheme();
        }
        catch (URISyntaxException e) {
            log.warn("Form href is invalid: ", e);
            return null;
        }
    }

    public List<Operation> getOp() {
        return op;
    }

    public String getSubprotocol() {
        return subprotocol;
    }

    public String getContentType() {
        return contentType;
    }

    @JsonAnyGetter
    public Map<String, Object> getOptionalProperties() {
        return optionalProperties;
    }

    @JsonAnySetter
    private void setOptionalForJackson(String name, String value) {
        getOptionalProperties().put(name, value);
    }

    public Object getOptional(String name) {
        return optionalProperties.get(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Form)) {
            return false;
        }
        Form form = (Form) o;
        return Objects.equals(href, form.href) &&
                Objects.equals(op, form.op) &&
                Objects.equals(subprotocol, form.subprotocol) &&
                Objects.equals(contentType, form.contentType) &&
                Objects.equals(optionalProperties, form.optionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(href, op, subprotocol, contentType, optionalProperties);
    }

    /**
     * Allows building new {@link Form} objects.
     */
    public static class Builder implements ObjectBuilder<Form> {
        private String href;
        private List<Operation> op;
        private String subprotocol;
        private String contentType;
        private Map<String, Object> optionalProperties = new HashMap<>();

        public Builder(Form form) {
            href = form.getHref();
            op = form.getOp();
            subprotocol = form.getSubprotocol();
            contentType = form.getContentType();
            optionalProperties = form.getOptionalProperties();
        }

        public Builder() {
            op = new ArrayList<>();
            optionalProperties = new HashMap<>();
        }

        public Builder setHref(String href) {
            this.href = href;
            return this;
        }

        @JsonSetter
        public Builder setOp(List<Operation> op) {
            this.op = op;
            return this;
        }

        public Builder setOp(Operation ... op) {
            return setOp(new ArrayList<>(Arrays.asList(op)));
        }

        public Builder setOp(Operation op) {
            return setOp(new ArrayList<>(Arrays.asList(op)));
        }

        public Builder addOp(Operation op) {
            this.op.add(op);
            return this;
        }

        public Builder setSubprotocol(String subprotocol) {
            this.subprotocol = subprotocol;
            return this;
        }

        public Builder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder setOptionalProperties(Map<String, Object> optionalProperties) {
            this.optionalProperties = optionalProperties;
            return this;
        }

        public Builder setOptional(String name, Object value) {
            optionalProperties.put(name, value);
            return this;
        }

        public Form build() {
            Form form = new Form();
            form.href = href;
            form.op = op;
            form.subprotocol = subprotocol;
            form.contentType = contentType;
            form.optionalProperties = optionalProperties;
            return form;
        }
    }
}
