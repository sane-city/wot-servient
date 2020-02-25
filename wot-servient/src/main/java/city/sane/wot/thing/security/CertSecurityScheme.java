package city.sane.wot.thing.security;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Certificate-based asymmetric key security configuration conformant with X509V3 identified by the
 * term cert (i.e., "scheme": "cert").<br> See also: https://www.w3.org/2019/wot/security#certsecurityscheme
 */
public class CertSecurityScheme implements SecurityScheme {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String identity;

    public CertSecurityScheme(String identity) {
        this.identity = identity;
    }

    public String getIdentity() {
        return identity;
    }

    @Override
    public String toString() {
        return "CertSecurityScheme{" +
                "identity='" + identity + '\'' +
                '}';
    }
}
