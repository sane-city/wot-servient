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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Describes properties of a security mechanism (e.g. password authentication).<br> See also:
 * https://www.w3.org/TR/wot-thing-description/#security-serialization-json
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "scheme")
@JsonSubTypes({
        @JsonSubTypes.Type(value = APIKeySecurityScheme.class, name = "apikey"),
        @JsonSubTypes.Type(value = BasicSecurityScheme.class, name = "basic"),
        @JsonSubTypes.Type(value = BearerSecurityScheme.class, name = "bearer"),
        @JsonSubTypes.Type(value = CertSecurityScheme.class, name = "cert"),
        @JsonSubTypes.Type(value = DigestSecurityScheme.class, name = "digest"),
        @JsonSubTypes.Type(value = NoSecurityScheme.class, name = "nosec"),
        @JsonSubTypes.Type(value = OAuth2SecurityScheme.class, name = "oauth2"),
        @JsonSubTypes.Type(value = PoPSecurityScheme.class, name = "pop"),
        @JsonSubTypes.Type(value = PSKSecurityScheme.class, name = "psk"),
        @JsonSubTypes.Type(value = PublicSecurityScheme.class, name = "public")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public interface SecurityScheme {
}
