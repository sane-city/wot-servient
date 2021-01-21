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

/**
 * Digest authentication security configuration identified by the term digest (i.e., "scheme":
 * "digest"). This scheme is similar to basic authentication but with added features to avoid
 * man-in-the-middle attacks.<br> See also: https://www.w3.org/2019/wot/security#digestsecurityscheme
 */
public class DigestSecurityScheme implements SecurityScheme {
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
