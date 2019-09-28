package city.sane.wot.binding.coap;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import city.sane.wot.thing.schema.StringSchema;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Allows consuming Things via CoAP.
 */
public class CoapProtocolClient implements ProtocolClient {
    final static Logger log = LoggerFactory.getLogger(CoapProtocolClient.class);
    private final ExecutorService executor;

    public CoapProtocolClient(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public CompletableFuture<Content> readResource(Form form) {
        CompletableFuture<Content> future = new CompletableFuture<>();

        String url = form.getHref();
        CoapClient client = new CoapClient(url)
                .setExecutor(executor)
                .setTimeout(10 * 1000);
        log.debug("CoapClient sending {} to {}", "GET", url);

        Request request = generateRequest(form, CoAP.Code.GET);
        client.advanced(new FutureCoapHandler(future), request);

        return future;
    }

    @Override
    public CompletableFuture<Content> writeResource(Form form, Content content) {
        CompletableFuture<Content> future = new CompletableFuture<>();

        String url = form.getHref();
        CoapClient client = new CoapClient(url)
                .setExecutor(executor)
                .setTimeout(10 * 1000);

        Request request = generateRequest(form, CoAP.Code.PUT);
        log.debug("Sending '{}' to '{}'", request.getCode(), url);
        if (content != null) {
            request.setPayload(content.getBody());
        }

        client.advanced(new FutureCoapHandler(future), request);

        return future;
    }

    @Override
    public CompletableFuture<Content> invokeResource(Form form, Content content) {
        CompletableFuture<Content> future = new CompletableFuture<>();

        String url = form.getHref();
        CoapClient client = new CoapClient(url)
                .setExecutor(executor)
                .setTimeout(10 * 1000);

        Request request = generateRequest(form, CoAP.Code.POST);
        log.debug("Sending '{}' to '{}'", request.getCode(), url);
        if (content != null) {
            request.setPayload(content.getBody());
        }

        client.advanced(new FutureCoapHandler(future), request);

        return future;
    }

    @Override
    public CompletableFuture<Subscription> subscribeResource(Form form, Observer<Content> observer) {
        String url = form.getHref();
        CoapClient client = new CoapClient(url)
                .setExecutor(executor);

        Subscription subscription = new Subscription();

        // Californium does not offer any method to wait until the observation is established...
        // This causes new values not being recognized directly after observation creation.
        // The client must wait "some" time before it can be sure that the observation is active.
        log.debug("CoapClient subscribe {}", url);
        client.observe(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                if (!subscription.isClosed()) {
                    String type = MediaTypeRegistry.toString(response.getOptions().getContentFormat());
                    byte[] body = response.getPayload();
                    Content output = new Content(type, body);
                    if (response.isSuccess()) {
                        log.debug("Next data received for subcription '{}'", url);
                        observer.next(output);
                    }
                    else {
                        subscription.unsubscribe();
                        try {
                            String error = ContentManager.contentToValue(output, new StringSchema());
                            log.debug("Error received for subcription '{}': ", url, error);
                        }
                        catch (ContentCodecException e) {
                            log.debug("Error received for subcription '{}': ", url, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onError() {
                if (!subscription.isClosed()) {
                    subscription.unsubscribe();
                    log.debug("Error received for subcription '{}'", url);
                }
            }
        });

        return CompletableFuture.completedFuture(subscription);
    }

    private Request generateRequest(Form form, CoAP.Code code, boolean observable) {
        Request request = new Request(code);

        if (form.getContentType() != null) {
            int mediaType = MediaTypeRegistry.parse(form.getContentType());
            if (mediaType != -1) {
                request.getOptions().setContentFormat(mediaType);
            }
        }

        if (observable) {
            request.setObserve();
        }

        return request;
    }

    private Request generateRequest(Form form, CoAP.Code code) {
        return generateRequest(form, code, false);
    }

    class FutureCoapHandler implements CoapHandler {
        private final CompletableFuture future;

        public FutureCoapHandler(CompletableFuture future) {
            this.future = future;
        }

        @Override
        public void onLoad(CoapResponse response) {
            log.debug("Response receivend: {}", response.getCode());
            String type = MediaTypeRegistry.toString(response.getOptions().getContentFormat());
            byte[] body = response.getPayload();
            Content output = new Content(type, body);
            if (response.isSuccess()) {
                future.complete(output);
            }
            else {
                try {
                    String error = ContentManager.contentToValue(output, new StringSchema());
                    future.completeExceptionally(new ProtocolClientException("Response was not successful: " + error));
                }
                catch (ContentCodecException e) {
                    future.completeExceptionally(new ProtocolClientException("Response was not successful: " + e.getMessage()));
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onError() {
            future.completeExceptionally(new ProtocolClientException("Response was not successful"));
        }
    }
}
