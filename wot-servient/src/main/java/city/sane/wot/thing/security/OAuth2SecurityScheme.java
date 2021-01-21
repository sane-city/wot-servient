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

import java.util.List;

/**
 * OAuth2 authentication security configuration for systems conformant with !RFC6749 and !RFC8252,
 * identified by the term oauth2 (i.e., "scheme": "oauth2"). For the implicit flow authorization
 * MUST be included. For the password and client flows token MUST be included. For the code flow
 * both authorization and token MUST be included. If no scopes are defined in the SecurityScheme
 * then they are considered to be empty.<br> See also: https://www.w3.org/2019/wot/security#oauth2securityscheme
 */
public class OAuth2SecurityScheme implements SecurityScheme {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String authorization;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String flow;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String token;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String refresh;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> scopes;

    public OAuth2SecurityScheme(String authorization,
                                String flow,
                                String token,
                                String refresh,
                                List<String> scopes) {
        this.authorization = authorization;
        this.flow = flow;
        this.token = token;
        this.refresh = refresh;
        this.scopes = scopes;
    }

    public String getAuthorization() {
        return authorization;
    }

    public String getFlow() {
        return flow;
    }

    public String getToken() {
        return token;
    }

    public String getRefresh() {
        return refresh;
    }

    public List<String> getScopes() {
        return scopes;
    }

    @Override
    public String toString() {
        return "OAuth2SecurityScheme{" +
                "authorization='" + authorization + '\'' +
                ", flow='" + flow + '\'' +
                ", token='" + token + '\'' +
                ", refresh='" + refresh + '\'' +
                ", scopes=" + scopes +
                '}';
    }
}
