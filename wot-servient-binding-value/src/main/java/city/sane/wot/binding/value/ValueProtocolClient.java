package city.sane.wot.binding.value;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.thing.content.Content;
import city.sane.wot.thing.form.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Allows consuming Things via values stored in Thing Descriptions.
 */
public class ValueProtocolClient implements ProtocolClient {
    final static Logger log = LoggerFactory.getLogger(ValueProtocolClient.class);

    @Override
    public CompletableFuture<Content> readResource(Form form) {
        String href = form.getHref();
        String value = href.replace("value:/", "");

        Content content = new Content("text/plain", value.getBytes());
        return CompletableFuture.completedFuture(content);
    }
}
