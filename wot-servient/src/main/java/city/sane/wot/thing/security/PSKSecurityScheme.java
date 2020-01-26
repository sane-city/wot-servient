package city.sane.wot.thing.security;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Pre-shared key authentication security configuration identified by the term psk (i.e., "scheme": "psk").<br>
 * See also: https://www.w3.org/2019/wot/security#psksecurityscheme
 */
public class PSKSecurityScheme extends SecurityScheme {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String identity;

    public PSKSecurityScheme(String identity) {
        this.identity = identity;
    }

    public String getIdentity() {
        return identity;
    }
}
