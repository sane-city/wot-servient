package city.sane.wot.binding.http.route;

import city.sane.wot.Servient;
import city.sane.wot.content.Content;
import city.sane.wot.thing.ExposedThing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReadAllPropertiesRouteTest {
    private Servient servient;
    private Map<String, ExposedThing> things;
    private Request request;
    private HttpServletRequest servletRequest;
    private Response response;
    private ExposedThing exposedThing;

    @BeforeEach
    public void setUp() {
        servient = mock(Servient.class);
        things = mock(Map.class);
        request = mock(Request.class);
        servletRequest = mock(HttpServletRequest.class);
        response = mock(Response.class);
        exposedThing = mock(ExposedThing.class);
    }

    @Test
    public void handleShouldPromptBasicAuthForBaseSecurityScheme() {
        when(request.raw()).thenReturn(servletRequest);
        when(request.params(":id")).thenReturn("counter");
        when(things.get(any())).thenReturn(exposedThing);

        ReadAllPropertiesRoute route = new ReadAllPropertiesRoute(servient, "Basic", things);

        assertEquals("Unauthorized", route.handle(request, response));
        verify(response).header(eq("WWW-Authenticate"), eq("Basic realm=\"counter\""));
        verify(response).status(401);
    }

    @Test
    public void handleShouldGrantAccessAfterSuccessfulBasicAuthForBaseSecurityScheme() {
        when(request.raw()).thenReturn(servletRequest);
        when(request.params(":id")).thenReturn("counter");
        when(request.headers("Authorization")).thenReturn("Basic Zm9vOmJhcg==");
        when(things.get(any())).thenReturn(exposedThing);
        when(servient.getCredentials(any())).thenReturn(Map.of("username", "foo", "password", "bar"));
        when(exposedThing.readProperties()).thenReturn(completedFuture(Map.of()));

        ReadAllPropertiesRoute route = new ReadAllPropertiesRoute(servient, "Basic", things);

        assertEquals(
                new Content("application/json", new byte[]{
                        0x7B,
                        0x7D
                }), route.handle(request, response));
    }

    @Test
    public void handleShouldGrantAccessAfterSuccessfulBearerAuthForBearerSecurityScheme() {
        when(request.raw()).thenReturn(servletRequest);
        when(request.params(":id")).thenReturn("counter");
        when(request.headers("Authorization")).thenReturn("Bearer iez0ic8Xohbu");
        when(things.get(any())).thenReturn(exposedThing);
        when(servient.getCredentials(any())).thenReturn(Map.of("token", "iez0ic8Xohbu"));
        when(exposedThing.readProperties()).thenReturn(completedFuture(Map.of()));

        ReadAllPropertiesRoute route = new ReadAllPropertiesRoute(servient, "Bearer", things);

        assertEquals(
                new Content("application/json", new byte[]{
                        0x7B,
                        0x7D
                }), route.handle(request, response));
    }
}
