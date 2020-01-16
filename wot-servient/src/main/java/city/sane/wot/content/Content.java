package city.sane.wot.content;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents any serialized content. Enables the transfer of arbitrary data structures.
 */
public class Content implements Serializable {
    public static final Content EMPTY_CONTENT = new Content(ContentManager.DEFAULT, new byte[0]);

    private final String type;
    private final byte[] body;

    public Content(String type, byte[] body) {
        this.type = type;
        this.body = body;
    }

    @Override
    public String toString() {
        return "Content [type=" + getType() + ", body=" + getBody() + "]";
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

    public String getType() {
        return type;
    }

    public byte[] getBody() {
        return body;
    }
}
