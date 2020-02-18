package city.sane.wot.thing.security;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Digest authentication security configuration identified by the term digest (i.e., "scheme":
 * "digest"). This scheme is similar to basic authentication but with added features to avoid
 * man-in-the-middle attacks.<br> See also: https://www.w3.org/2019/wot/security#digestsecurityscheme
 */
public class DigestSecurityScheme extends SecurityScheme {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String in;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String qop;

    public DigestSecurityScheme(String in, String name, String qop) {
        this.in = in;
        this.name = name;
        this.qop = qop;
    }

    public String getIn() {
        return in;
    }

    public String getName() {
        return name;
    }

    public String getQop() {
        return qop;
    }

    @Override
    public String toString() {
        return "DigestSecurityScheme{" +
                "in='" + in + '\'' +
                ", name='" + name + '\'' +
                ", qop='" + qop + '\'' +
                '}';
    }
}
