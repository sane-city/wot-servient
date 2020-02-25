package city.sane.wot.thing.security;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * Basic authentication security configuration identified by the term basic (i.e., "scheme":
 * "basic"), using an unencrypted username and password. This scheme should be used with some other
 * security mechanism providing confidentiality, for example, TLS.<br> See also:
 * https://www.w3.org/2019/wot/security#basicsecurityscheme
 */
public class BasicSecurityScheme implements SecurityScheme {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final String in;

    public BasicSecurityScheme() {
        this(null);
    }

    public BasicSecurityScheme(String in) {
        this.in = in;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIn());
    }

    public String getIn() {
        return in;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BasicSecurityScheme)) {
            return false;
        }
        BasicSecurityScheme that = (BasicSecurityScheme) o;
        return Objects.equals(getIn(), that.getIn());
    }

    @Override
    public String toString() {
        return "BasicSecurityScheme{" +
                "in='" + in + '\'' +
                '}';
    }
}
