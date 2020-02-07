package city.sane.wot.thing.security;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * API key authentication security configuration identified by the term apikey (i.e., "scheme":
 * "apikey"). This is for the case where the access token is opaque and is not using a standard
 * token format.<br> See also: https://www.w3.org/2019/wot/security#apikeysecurityscheme
 */
public class APIKeySecurityScheme extends SecurityScheme {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String in;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    public APIKeySecurityScheme(String in, String name) {
        this.in = in;
        this.name = name;
    }

    public String getIn() {
        return in;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "APIKeySecurityScheme{" +
                "in='" + in + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
