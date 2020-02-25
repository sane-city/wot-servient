package city.sane.wot.binding.value;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Allows consuming Things via values stored in Thing Descriptions.
 */
public class ValueProtocolClient implements ProtocolClient {
    @Override
    public CompletableFuture<Content> readResource(Form form) {
        String href = form.getHref();
        String value = href.replace("value:/", "");

        Content content = new Content("text/plain", value.getBytes());
        return completedFuture(content);
    }
}
