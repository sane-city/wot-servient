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
 * Proof-of-possession (PoP) token authentication security configuration identified by the term pop
 * (i.e., "scheme": "pop"). Here jwt indicates conformance with !RFC7519, jws indicates conformance
 * with !RFC7797, cwt indicates conformance with !RFC8392, and jwe indicates conformance with
 * RFC7516, with values for alg interpreted consistently with those standards. Other formats and
 * algorithms for PoP tokens MAY be specified in vocabulary extensions.<br> See also:
 * https://www.w3.org/2019/wot/security#popsecurityscheme
 */
public class PoPSecurityScheme implements SecurityScheme {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String in;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String format;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String authorization;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String alg;

    public PoPSecurityScheme(String in,
                             String name,
                             String format,
                             String authorization,
                             String alg) {
        this.in = in;
        this.name = name;
        this.format = format;
        this.authorization = authorization;
        this.alg = alg;
    }

    public String getIn() {
        return in;
    }

    public String getName() {
        return name;
    }

    public String getFormat() {
        return format;
    }

    public String getAuthorization() {
        return authorization;
    }

    public String getAlg() {
        return alg;
    }

    @Override
    public String toString() {
        return "PoPSecurityScheme{" +
                "in='" + in + '\'' +
                ", name='" + name + '\'' +
                ", format='" + format + '\'' +
                ", authorization='" + authorization + '\'' +
                ", alg='" + alg + '\'' +
                '}';
    }
}
