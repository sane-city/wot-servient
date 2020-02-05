package city.sane.wot.thing.security;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Bearer token authentication security configuration identified by the term bearer (i.e., "scheme": "bearer"). This scheme is intended for
 * situations where bearer tokens are used independently of OAuth2. If the oauth2 scheme is specified it is not generally necessary to
 * specify this scheme as well as it is implied. For format, the value jwt indicates conformance with RFC7519, jws indicates conformance
 * with RFC7797, cwt indicates conformance with RFC8392, and jwe indicates conformance with !RFC7516, with values for alg interpreted
 * consistently with those standards. Other formats and algorithms for bearer tokens MAY be specified in vocabulary extensions.<br>
 * See also: https://www.w3.org/2019/wot/security#bearersecurityscheme
 */
public class BearerSecurityScheme extends SecurityScheme {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String in;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String alg;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String format;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String authorization;

    public BearerSecurityScheme(String in, String alg, String format, String name, String authorization) {
        this.in = in;
        this.alg = alg;
        this.format = format;
        this.name = name;
        this.authorization = authorization;
    }

    public String getIn() {
        return in;
    }

    public String getAlg() {
        return alg;
    }

    public String getFormat() {
        return format;
    }

    public String getName() {
        return name;
    }

    public String getAuthorization() {
        return authorization;
    }

    @Override
    public String toString() {
        return "BearerSecurityScheme{" +
                "in='" + in + '\'' +
                ", alg='" + alg + '\'' +
                ", format='" + format + '\'' +
                ", name='" + name + '\'' +
                ", authorization='" + authorization + '\'' +
                '}';
    }
}
