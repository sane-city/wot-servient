package city.sane.wot.binding.http;

import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.security.BasicSecurityScheme;
import city.sane.wot.thing.security.BearerSecurityScheme;
import city.sane.wot.thing.security.SecurityScheme;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class HttpProtocolClientTest {
    private HttpClient requestClient;
    private Form form;
    private HttpResponse httpResponse;
    private StatusLine statusLine;
    private HttpEntity httpEntity;
    private HttpProtocolClient client;
    private SecurityScheme securityScheme;

    @Before
    public void setUp() {
        requestClient = mock(HttpClient.class);
        form = mock(Form.class);
        httpResponse = mock(HttpResponse.class);
        statusLine = mock(StatusLine.class);
        httpEntity = mock(HttpEntity.class);
        securityScheme = mock(SecurityScheme.class);
        client = new HttpProtocolClient(requestClient);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void readResourceShouldCreateProperRequest() throws IOException {
        when(form.getHref()).thenReturn("http://localhost/foo");
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(requestClient.execute(any())).thenReturn(httpResponse);

        client.readResource(form).join();

        HttpUriRequest request = RequestBuilder.create("GET").setUri("http://localhost/foo").build();
        verify(requestClient, times(1)).execute(argThat(new HttpUriRequestMatcher(request)));
    }

    @Test
    public void readResourceShouldCreateProperRequestWithBasicAuth() throws IOException {
        when(form.getHref()).thenReturn("http://localhost/foo");
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(requestClient.execute(any())).thenReturn(httpResponse);

        Map credentials = Map.of(
                "username", "foo",
                "password", "bar"
        );
        client.setSecurity(List.of(new BasicSecurityScheme()), credentials);
        client.readResource(form).join();

        HttpUriRequest request = RequestBuilder.create("GET")
                .setUri("http://localhost/foo")
                .addHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.encodeBase64(("foo:bar").getBytes())))
                .build();
        verify(requestClient, times(1)).execute(argThat(new HttpUriRequestMatcher(request)));
    }

    @Test
    public void readResourceShouldCreateProperRequestWithBearerToken() throws IOException {
        when(form.getHref()).thenReturn("http://localhost/foo");
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(requestClient.execute(any())).thenReturn(httpResponse);

        Map credentials = Map.of(
                "token", "achu6Ahx"
        );
        client.setSecurity(List.of(new BearerSecurityScheme()), credentials);
        client.readResource(form).join();

        HttpUriRequest request = RequestBuilder.create("GET")
                .setUri("http://localhost/foo")
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer achu6Ahx")
                .build();
        verify(requestClient, times(1)).execute(argThat(new HttpUriRequestMatcher(request)));
    }

    private class HttpUriRequestMatcher extends ArgumentMatcher<HttpUriRequest> {
        private final HttpUriRequest left;

        public HttpUriRequestMatcher(HttpUriRequest left) {
            this.left = left;
        }

        @Override
        public boolean matches(Object o) {
            if (o instanceof HttpUriRequest) {
                return matches((HttpUriRequest) o);
            }
            else {
                return false;
            }
        }

        public boolean matches(HttpUriRequest right) {
            if (left == right) {
                return true;
            }
            if (right == null) {
                return false;
            }
            else {
                return left.getMethod().equals(right.getMethod()) &&
                        left.getURI().equals(right.getURI()) &&
                        Arrays.equals(left.getAllHeaders(), right.getAllHeaders(), (a, b) -> new HttpHeaderMatcher(a).matches(b) ? 0 : -1);
            }
        }
    }

    private class HttpHeaderMatcher extends ArgumentMatcher<Header> {
        private final Header left;

        public HttpHeaderMatcher(Header left) {
            this.left = left;
        }

        @Override
        public boolean matches(Object o) {
            if (o instanceof Header) {
                return matches((Header) o);
            }
            else {
                return false;
            }
        }

        public boolean matches(Header right) {
            if (left == right) {
                return true;
            }
            if (right == null) {
                return false;
            }
            else {
                if (left instanceof BasicHeader && right instanceof BasicHeader) {
                    BasicHeader basicLeft = (BasicHeader) left;
                    BasicHeader basicRight = (BasicHeader) right;

                    return basicLeft.getName().equals(basicRight.getName()) &&
                            basicLeft.getValue().equals(basicRight.getValue());
                }
                else {
                    throw new RuntimeException("HttpHeaderMatcher does not support this header type");
                }
            }
        }
    }
}