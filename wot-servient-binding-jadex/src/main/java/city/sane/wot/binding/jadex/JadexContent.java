package city.sane.wot.binding.jadex;

import city.sane.wot.content.Content;

/**
 * This class is needed so that Jadex can correctly (de)serialize {@link Content} objects and send
 * them to other Jadex platforms.
 */
class JadexContent {
    private String type;
    private byte[] body;

    public JadexContent(Content content) {
        this(content.getType(), content.getBody());
    }

    public JadexContent(String type, byte[] body) {
        this.type = type;
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public Content fromJadex() {
        return new Content(type, body);
    }
}
