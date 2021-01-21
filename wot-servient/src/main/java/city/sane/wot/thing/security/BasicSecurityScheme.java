/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
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
