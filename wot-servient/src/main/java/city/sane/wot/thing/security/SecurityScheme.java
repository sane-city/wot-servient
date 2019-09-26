package city.sane.wot.thing.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Describes properties of a security mechanism (e.g. password authentication).<br>
 * See also: https://www.w3.org/TR/wot-thing-description/#security-serialization-json
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityScheme {
    private String scheme;

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
}
