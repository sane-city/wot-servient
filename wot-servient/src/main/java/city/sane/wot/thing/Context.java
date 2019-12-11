package city.sane.wot.thing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a JSON-LD context.
 */
@JsonDeserialize(using = ContextDeserializer.class)
@JsonSerialize(using = ContextSerializer.class)
public class Context implements Serializable {
    private final Map<String, String> urls = new HashMap<>();

    public Context() {
    }

    public Context(String url) {
        addContext(url);
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
        return "Context [urls=" + urls + "]";
    }

    public Context addContext(String prefix, String url) {
        urls.put(prefix, url);
        return this;
    }

    public Context addContext(String url) {
        return addContext(null, url);
    }

    public String getUrl(String prefix) {
        return urls.get(prefix);
    }

    public String getDefaultUrl() {
        return getUrl(null);
    }

    public Map<String, String> getPrefixedUrls() {
        return urls.entrySet().stream().filter(e -> e.getKey() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, String> getUrls() {
        return urls;
    }
}
