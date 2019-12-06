package city.sane.wot.content;

/**
 * (De)serializes data in HTML format.
 */
public class HtmlCodec extends TextCodec {
    @Override
    public String getMediaType() {
        return "text/html";
    }
}
