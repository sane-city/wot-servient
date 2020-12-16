package city.sane.wot.binding.http.route;

import city.sane.wot.Servient;
import city.sane.wot.thing.ExposedThing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubscribeEventRouteTest {
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
        when(request.params(":id")).thenReturn("counter");
        when(things.get(any())).thenReturn(exposedThing);

        SubscribeEventRoute route = new SubscribeEventRoute(servient, "Basic", things);

        assertEquals("Unauthorized", route.handle(request, response));
        verify(response).header(eq("WWW-Authenticate"), eq("Basic realm=\"counter\""));
        verify(response).status(401);
    }

    @Test
    public void handleShouldGrantAccessAfterSuccessfulBasicAuthForBaseSecurityScheme() {
        when(request.params(":id")).thenReturn("counter");
        when(request.headers("Authorization")).thenReturn("Basic Zm9vOmJhcg==");
        when(things.get(any())).thenReturn(exposedThing);
        when(servient.getCredentials(any())).thenReturn(Map.of("username", "foo", "password", "bar"));

        SubscribeEventRoute route = new SubscribeEventRoute(servient, "Basic", things);

        assertEquals("Event not found", route.handle(request, response));
        verify(response).status(404);
    }

    @Test
    public void handleShouldGrantAccessAfterSuccessfulBearerAuthForBearerSecurityScheme() {
        when(request.params(":id")).thenReturn("counter");
        when(request.headers("Authorization")).thenReturn("Bearer iez0ic8Xohbu");
        when(things.get(any())).thenReturn(exposedThing);
        when(servient.getCredentials(any())).thenReturn(Map.of("token", "iez0ic8Xohbu"));

        SubscribeEventRoute route = new SubscribeEventRoute(servient, "Bearer", things);

        assertEquals("Event not found", route.handle(request, response));
        verify(response).status(404);
    }
}