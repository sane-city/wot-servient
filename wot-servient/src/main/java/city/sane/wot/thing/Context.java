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
package city.sane.wot.thing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a JSON-LD context.
 */
@JsonDeserialize(using = ContextDeserializer.class)
@JsonSerialize(using = ContextSerializer.class)
public class Context {
    private final Map<String, String> urls = new HashMap<>();

    public Context() {
    }

    public Context(String url) {
        addContext(url);
    }

    public Context addContext(String url) {
        return addContext(null, url);
    }

    public Context addContext(String prefix, String url) {
        urls.put(prefix, url);
        return this;
    }

    public Context(String prefix, String url) {
        addContext(prefix, url);
    }

    @Override
    public int hashCode() {
        return getUrls().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Context)) {
            return false;
        }
        return getUrls().equals(((Context) obj).getUrls());
    }

    @Override
    public String toString() {
        return "Context{" +
                "urls=" + urls +
                '}';
    }

    private Map<String, String> getUrls() {
        return urls;
    }

    public String getDefaultUrl() {
        return getUrl(null);
    }

    public String getUrl(String prefix) {
        return urls.get(prefix);
    }

    public Map<String, String> getPrefixedUrls() {
        return urls.entrySet().stream().filter(e -> e.getKey() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
