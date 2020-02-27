package city.sane.wot.content;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents any serialized content. Enables the transfer of arbitrary data structures.
 */
public class Content {
    public static final Content EMPTY_CONTENT = new Content(ContentManager.DEFAULT, new byte[0]);
    private final String type;
    private final byte[] body;

    private Content() {
        type = null;
        body = null;
    }

    public Content(byte[] body) {
        this(ContentManager.DEFAULT, body);
    }

    public Content(String type, byte[] body) {
        this.type = type;
        this.body = body;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), Arrays.hashCode(getBody()));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Content)) {
            return false;
        }
        return Objects.equals(getType(), ((Content) obj).getType()) && Arrays.equals(getBody(), ((Content) obj).getBody());
    }

    @Override
    public String toString() {
        return "Content{" +
                "type='" + type + '\'' +
                ", body=" + Arrays.toString(body) +
                ", new String(body)=" + new String(body) +
                '}';
    }

    public String getType() {
        return type;
    }

    public byte[] getBody() {
        return body;
    }
}
