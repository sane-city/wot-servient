package city.sane.wot.thing.security;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Raw public key asymmetric key security configuration identified by the term public (i.e., "scheme": "public").<br>
 * See also: https://www.w3.org/2019/wot/security#publicsecurityscheme
 */
public class PublicSecurityScheme extends SecurityScheme {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String identity;

    public PublicSecurityScheme(String identity) {
        this.identity = identity;
    }

    public String getIdentity() {
        return identity;
    }

    @Override
    public String toString() {
        return "PublicSecurityScheme{" +
                "identity='" + identity + '\'' +
                '}';
    }
}
