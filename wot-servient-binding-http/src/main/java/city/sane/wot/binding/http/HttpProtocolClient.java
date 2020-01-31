package city.sane.wot.binding.http;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import city.sane.wot.thing.security.BasicSecurityScheme;
import city.sane.wot.thing.security.NoSecurityScheme;
import city.sane.wot.thing.security.SecurityScheme;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Allows consuming Things via HTTP.
 */
public class HttpProtocolClient implements ProtocolClient {
    private static final Logger log = LoggerFactory.getLogger(HttpProtocolClient.class);
    private static final String HTTP_METHOD_NAME = "htv:methodName";
    private static final Duration LONG_POLLING_TIMEOUT = Duration.ofMinutes(60);
    private final HttpClient requestClient;
    private String authorization = null;

    HttpProtocolClient(HttpClient requestClient) {
        this.requestClient = requestClient;
    }

    public HttpProtocolClient() {
        this(HttpClientBuilder.create().build());
    }

    @Override
    public CompletableFuture<Content> readResource(Form form) {
        return CompletableFuture.supplyAsync(() -> {
            HttpUriRequest request = generateRequest(form);
            log.debug("Sending '{}' to '{}'", request.getMethod(), request.getURI());
            try {
                HttpResponse response = requestClient.execute(request);
                return checkResponse(response);

            }
            catch (IOException | ProtocolClientException e) {
                throw new CompletionException(new ProtocolClientException("Error during http request: " + e.getMessage()));
            }
        });
    }

    @Override
    public CompletableFuture<Content> writeResource(Form form, Content content) {
        return CompletableFuture.supplyAsync(() -> {
            HttpUriRequest request = generateRequest(form, "PUT", content);

            log.debug("Sending '{}' to '{}'", request.getMethod(), request.getURI());
            try {
                HttpResponse response = requestClient.execute(request);
                return checkResponse(response);

            }
            catch (IOException | ProtocolClientException e) {
                throw new CompletionException(new ProtocolClientException("Error during http request: " + e.getMessage()));
            }
        });
    }

    @Override
    public CompletableFuture<Content> invokeResource(Form form, Content content) {
        return CompletableFuture.supplyAsync(() -> {
            HttpUriRequest request = generateRequest(form, "POST", content);

            log.debug("Sending '{}' to '{}'", request.getMethod(), request.getURI());
            try {
                HttpResponse response = requestClient.execute(request);
                return checkResponse(response);

            }
            catch (IOException | ProtocolClientException e) {
                throw new CompletionException(new ProtocolClientException("Error during http request: " + e.getMessage()));
            }
        });
    }

    @Override
    public CompletableFuture<Subscription> subscribeResource(Form form, Observer<Content> observer) {
        HttpUriRequest request = generateRequest(form);

        // long timeout for long polling
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout((int) LONG_POLLING_TIMEOUT.toMillis())
                .setConnectionRequestTimeout((int) LONG_POLLING_TIMEOUT.toMillis())
                .setSocketTimeout((int) LONG_POLLING_TIMEOUT.toMillis()).build();

        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        Subscription subscription = new Subscription(() -> {
            log.debug("Cancel subscription for '{}' to '{}'", request.getMethod(), request.getURI());
            try {
                client.close();
            }
            catch (IOException e) {
                // ignore
            }
        });

        CompletableFuture.runAsync(() -> {
            while (!subscription.isClosed()) {
                log.debug("Sending '{}' to '{}'", request.getMethod(), request.getURI());
                try {
                    HttpResponse response = client.execute(request);

                    if (!subscription.isClosed()) {
                        Content content = checkResponse(response);
                        log.debug("Next data received for Event connection");
                        observer.next(content);
                    }
                }
                catch (IOException | ProtocolClientException e) {
                    if (!subscription.isClosed()) {
                        log.warn("Error received for Event connection", e);

                        observer.error(e);
                        subscription.unsubscribe();
                    }
                }
            }
        });

        return CompletableFuture.completedFuture(subscription);
    }

    @Override
    public boolean setSecurity(List<SecurityScheme> metadata, Object credentials) {
        if (metadata.isEmpty()) {
            log.warn("HttpClient without security");
            return false;
        }

        // TODO: add support for multiple security schemes
        SecurityScheme security = metadata.get(0);

        if (security instanceof BasicSecurityScheme) {
            Map<String, String> credentialsMap = (Map) credentials;
            String username = credentialsMap.get("username");
            String password = credentialsMap.get("password");
            authorization = "Basic " + new String(Base64.encodeBase64((username + ":" + password).getBytes()));
            return true;
        }
        else if (security instanceof NoSecurityScheme) {
            // nothing to do
            return true;
        }
        else {
            log.error("HttpClient cannot set security scheme '{}'", security);
            return false;
        }
    }

    private HttpUriRequest generateRequest(Form form, String defaultMethod, Content content) {
        String href = form.getHref();
        String method = defaultMethod;
        if (form.getOptional(HTTP_METHOD_NAME) != null) {
            method = (String) form.getOptional(HTTP_METHOD_NAME);
        }

        RequestBuilder builder = RequestBuilder.create(method)
                .setUri(href);

        if (authorization != null) {
            String authorizationHeader = HttpHeaders.AUTHORIZATION;
            builder.addHeader(authorizationHeader, authorization);
        }

        if (content != null) {
            builder.setHeader("Content-Type", content.getType());
            byte[] body = content.getBody();
//            builder.setHeader("Content-Length", Integer.toString(body.length));
            builder.setEntity(new ByteArrayEntity(body));
        }

        return builder.build();
    }

    private HttpUriRequest generateRequest(Form form, Content content) {
        return generateRequest(form, "GET", content);
    }

    private HttpUriRequest generateRequest(Form form) {
        return generateRequest(form, null);
    }

    private Content checkResponse(HttpResponse response) throws ProtocolClientException {
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();

        if (statusCode < HttpStatus.SC_OK) {
            throw new ProtocolClientException("Received '" + statusCode + "' and cannot continue (not implemented)");
        }
        else if (statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
            HttpEntity entity = response.getEntity();

            ContentType contentType = ContentType.get(entity);
            String type = null;
            if (contentType != null) {
                type = contentType.toString();
            }

            try {
                byte[] body;
                if (entity != null) {
                    InputStream inputStream = entity.getContent();
                    body = inputStream.readAllBytes();
                }
                else {
                    body = new byte[0];
                }
                return new Content(type, body);
            }
            catch (IOException e) {
                throw new ProtocolClientException("Error during http request: " + e.getMessage());
            }
        }
        else if (statusCode < HttpStatus.SC_BAD_REQUEST) {
            throw new ProtocolClientException("Received '" + statusCode + "' and cannot continue (not implemented)");
        }
        else if (statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            throw new ProtocolClientException("Client error: " + statusLine.toString());
        }
        else {
//            String body = EntityUtils.toString(response.getEntity());
            throw new ProtocolClientException("Server error: " + statusLine.toString());
        }
    }
}
