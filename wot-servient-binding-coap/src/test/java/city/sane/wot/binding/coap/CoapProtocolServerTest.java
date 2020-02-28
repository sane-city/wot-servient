package city.sane.wot.binding.coap;

import city.sane.wot.Servient;
import city.sane.wot.binding.coap.resource.ThingResource;
import city.sane.wot.thing.ExposedThing;
import org.eclipse.californium.core.CoapResource;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CoapProtocolServerTest {
    private String bindHost;
    private int bindPort;
    private List<String> addresses;
    private Map<String, ExposedThing> things;
    private Map<String, CoapResource> resources;
    private Supplier<WotCoapServer> serverSupplier;
    private Servient servient;
    private WotCoapServer coapServer;
    private ExposedThing thing;
    private CoapResource resource;

    @Before
    public void setUp() {
        bindHost = "0.0.0.0";
        bindPort = 5683;
        addresses = mock(List.class);
        things = mock(Map.class);
        resources = mock(Map.class);
        serverSupplier = mock(Supplier.class);
        servient = mock(Servient.class);
        coapServer = mock(WotCoapServer.class);
        thing = mock(ExposedThing.class);
        resource = mock(CoapResource.class);
    }

    @Test
    public void startShouldCreateAndStartServer() {
        when(serverSupplier.get()).thenReturn(coapServer);

        CoapProtocolServer server = new CoapProtocolServer(bindHost, bindPort, addresses, things, resources, serverSupplier, null);
        server.start(servient);

        verify(serverSupplier, timeout(1 * 1000L).times(1)).get();
        verify(coapServer, timeout(1 * 1000L).times(1)).start();
    }

    @Test
    public void stopShouldStopAndDestroyServer() {
        CoapProtocolServer server = new CoapProtocolServer(bindHost, bindPort, addresses, things, resources, serverSupplier, coapServer);
        server.stop();

        verify(coapServer, timeout(1 * 1000L).times(1)).stop();
        verify(coapServer, timeout(1 * 1000L).times(1)).destroy();
    }

    @Test
    public void exposeShouldCreateThingResource() {
        when(thing.getId()).thenReturn("counter");

        CoapProtocolServer server = new CoapProtocolServer(bindHost, bindPort, addresses, things, resources, serverSupplier, coapServer);
        server.expose(thing);

        verify(resources, timeout(1 * 1000L).times(1)).put(eq("counter"), any(ThingResource.class));
    }

    @Test
    public void destroyShouldRemoveThingResource() {
        when(resources.remove(any())).thenReturn(resource);
        when(coapServer.getRoot()).thenReturn(resource);

        CoapProtocolServer server = new CoapProtocolServer(bindHost, bindPort, addresses, things, resources, serverSupplier, coapServer);
        server.destroy(thing);

        verify(resource, timeout(1 * 1000L).times(1)).delete(resource);
    }

    @Test
    public void getDirectoryUrlShouldReturnFristAddress() throws URISyntaxException {
        when(addresses.get(0)).thenReturn("coap://0.0.0.0");

        CoapProtocolServer server = new CoapProtocolServer(bindHost, bindPort, addresses, things, resources, serverSupplier, coapServer);

        assertEquals(new URI("coap://0.0.0.0"), server.getDirectoryUrl());
    }

    @Test
    public void getThingUrl() throws URISyntaxException {
        when(addresses.get(0)).thenReturn("coap://0.0.0.0");

        CoapProtocolServer server = new CoapProtocolServer(bindHost, bindPort, addresses, things, resources, serverSupplier, coapServer);

        assertEquals(new URI("coap://0.0.0.0/counter"), server.getThingUrl("counter"));
    }
}