package city.sane.wot.binding.http.route;

import city.sane.wot.Servient;
import city.sane.wot.thing.ExposedThing;
import org.junit.Before;
import org.junit.Test;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class InvokeActionRouteTest {
    private Servient servient;
    private Map<String, ExposedThing> things;
    private Request request;
    private HttpServletRequest servletRequest;
    private Response response;
    private ExposedThing exposedThing;

    @Before
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

        InvokeActionRoute route = new InvokeActionRoute(servient, "Basic", things);

        assertEquals("Unauthorized", route.handle(request, response));
        verify(response, times(1)).header(eq("WWW-Authenticate"), eq("Basic realm=\"counter\""));
        verify(response, times(1)).status(401);
    }

    @Test
    public void handleShouldGrantAccessAfterSuccessfulBasicAuthForBaseSecurityScheme() {
        when(request.raw()).thenReturn(servletRequest);
        when(request.params(":id")).thenReturn("counter");
        when(request.headers("Authorization")).thenReturn("Basic Zm9vOmJhcg==");
        when(things.get(any())).thenReturn(exposedThing);
        when(servient.getCredentials(any())).thenReturn(Map.of("username", "foo", "password", "bar"));

        InvokeActionRoute route = new InvokeActionRoute(servient, "Basic", things);

        assertEquals("Action not found", route.handle(request, response));
        verify(response, times(1)).status(404);
    }

    @Test
    public void handleShouldGrantAccessAfterSuccessfulBearerAuthForBearerSecurityScheme() {
        when(request.raw()).thenReturn(servletRequest);
        when(request.params(":id")).thenReturn("counter");
        when(request.headers("Authorization")).thenReturn("Bearer iez0ic8Xohbu");
        when(things.get(any())).thenReturn(exposedThing);
        when(servient.getCredentials(any())).thenReturn(Map.of("token", "iez0ic8Xohbu"));

        InvokeActionRoute route = new InvokeActionRoute(servient, "Bearer", things);

        assertEquals("Action not found", route.handle(request, response));
        verify(response, times(1)).status(404);
    }
}