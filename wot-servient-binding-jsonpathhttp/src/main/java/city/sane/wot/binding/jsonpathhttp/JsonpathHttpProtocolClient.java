package city.sane.wot.binding.jsonpathhttp;

import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.http.HttpProtocolClient;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Allows consuming Things via HTTP. The return values can be manipulated with JSON queries.
 */
public class JsonpathHttpProtocolClient extends HttpProtocolClient {
    static final Logger log = LoggerFactory.getLogger(JsonpathHttpProtocolClient.class);

    @Override
    public CompletableFuture<Content> readResource(Form form) {
        // create form with http protocol href
        Form httpForm = new Form.Builder(form)
                .setHref(form.getHref().replace("jsonpath+http", "http"))
                .build();

        return super.readResource(httpForm).thenApply(content -> {
            if (form.getOptional("sane:jsonPath") != null) {
                try {
                    // apply jsonpath on content
                    String json = new String(content.getBody());

                    String jsonPath = form.getOptional("sane:jsonPath").toString();
                    Object newValue = JsonPath.read(json, jsonPath);

                    Content newContent = ContentManager.valueToContent(newValue, "application/json");
                    return newContent;
                }
                catch (ContentCodecException e) {
                    log.warn("Unable apply JSONPath: {}", e);
                    return content;
                }
            }
            else {
                log.warn("No JsonPath found in form '{}'", form);
                return content;
            }
        });
    }

    @Override
    public CompletableFuture<Content> writeResource(Form form, Content content) {
        CompletableFuture<Content> future = new CompletableFuture<>();
        future.completeExceptionally(new ProtocolClientException("JsonpathHttpClient does not implement 'write'"));
        return future;
    }

    @Override
    public CompletableFuture<Content> invokeResource(Form form, Content content) {
        CompletableFuture<Content> future = new CompletableFuture<>();
        future.completeExceptionally(new ProtocolClientException("JsonpathHttpClient does not implement 'invoke'"));
        return future;
    }

    @Override
    public CompletableFuture<Subscription> subscribeResource(Form form, Observer<Content> observer) {
        observer.error(new ProtocolClientException("JsonpathHttpClient does not implement 'subscribe'"));
        return null;
    }

    @Override
    public CompletableFuture<Content> invokeResource(Form form) {
        return invokeResource(form, null);
    }
}
