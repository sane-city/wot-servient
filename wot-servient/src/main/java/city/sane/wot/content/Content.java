package city.sane.wot.content;

import java.io.Serializable;

/**
 * Represents any serialized content. Enables the transfer of arbitrary data structures.
 */
public class Content implements Serializable {
    private final String type;
    private final byte[] body;

    private Content(){
        type = null;
        body = null;
    }

    public Content(String type, byte[] body) {
        this.type = type;
        this.body = body;
    }

    @Override
    public String toString() {
        return "Content [type=" + getType() + ", body=" + getBody() + "]";
    }

    public String getType() {
        return type;
    }

    public byte[] getBody() {
        return body;
    }
}
