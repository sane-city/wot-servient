package city.sane.wot.thing.security;

/**
 * A security configuration corresponding to identified by the term nosec (i.e., "scheme": "nosec"),
 * indicating there is no authentication or other mechanism required to access the resource.<br> See
 * also: https://www.w3.org/2019/wot/security#nosecurityscheme
 */
public class NoSecurityScheme implements SecurityScheme {
    @Override
    public int hashCode() {
        return 42;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && getClass() == o.getClass();
    }

    @Override
    public String toString() {
        return "NoSecurityScheme{}";
    }
}
