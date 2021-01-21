/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package city.sane.wot;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.ProtocolClientFactory;
import city.sane.wot.binding.ProtocolClientNotImplementedException;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.ProtocolServerException;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.DiscoveryMethod;
import city.sane.wot.thing.filter.SparqlThingQuery;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.form.Form;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServientTest {
    private ServientConfig config;
    private ProtocolServer server;
    private ProtocolClientFactory clientFactory;
    private ProtocolClient client;
    private ExposedThing exposedThing;
    private Thing thing;

    @BeforeEach
    public void setUp() {
        config = mock(ServientConfig.class);
        server = mock(ProtocolServer.class);
        clientFactory = mock(ProtocolClientFactory.class);
        client = mock(ProtocolClient.class);
        exposedThing = mock(ExposedThing.class);
        thing = mock(Thing.class);
    }

    @Test
    public void start() throws ExecutionException, InterruptedException {
        Servient servient = new Servient(config);

        assertNull(servient.start().get());
    }

    @Test
    public void startFails() throws Throwable {
        when(server.start(any())).thenReturn(failedFuture(new ProtocolServerException()));
        when(config.getServers()).thenReturn(List.of(server));

        Servient servient = new Servient(config);

        assertThrows(ProtocolServerException.class, () -> {
            try {
                servient.start().get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void shutdown() throws ExecutionException, InterruptedException {
        Servient servient = new Servient(config);

        assertNull(servient.shutdown().get());
    }

    @Test
    public void shutdownFails() throws Throwable {
        when(server.stop()).thenReturn(failedFuture(new ProtocolServerException()));
        when(config.getServers()).thenReturn(List.of(server));

        Servient servient = new Servient(config);

        assertThrows(ProtocolServerException.class, () -> {
            try {
                servient.shutdown().get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void expose() throws InterruptedException, ExecutionException {
        when(server.expose(any())).thenReturn(completedFuture(null));

        Servient servient = new Servient(List.of(server), Map.of(), Map.of(), Map.of("counter", exposedThing));

        assertThat(servient.expose("counter").get(), is(exposedThing));
        verify(server).expose(exposedThing);
    }

    @Test
    public void exposeWithoutServers() throws Throwable {
        Servient servient = new Servient(List.of(), Map.of(), Map.of(), Map.of("counter", exposedThing));

        assertThrows(ServientException.class, () -> {
            try {
                servient.expose("counter").get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void exposeUnknownThing() throws Throwable {
        Servient servient = new Servient(List.of(), Map.of(), Map.of(), Map.of());

        assertThrows(ServientException.class, () -> {
            try {
                servient.expose("counter").get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void destroy() throws ExecutionException, InterruptedException {
        when(server.destroy(any())).thenReturn(completedFuture(null));

        Servient servient = new Servient(List.of(server), Map.of(), Map.of(), Map.of("counter", exposedThing));

        assertThat(servient.destroy("counter").get(), is(exposedThing));
        verify(server).destroy(exposedThing);
    }

    @Test
    public void destroyWithoutServers() throws Throwable {
        Servient servient = new Servient(List.of(), Map.of(), Map.of(), Map.of("counter", exposedThing));

        assertThrows(ServientException.class, () -> {
            try {
                servient.destroy("counter").get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void destroyUnknownThing() throws Throwable {
        Servient servient = new Servient(List.of(), Map.of(), Map.of(), Map.of());

        assertThrows(ServientException.class, () -> {
            try {
                servient.destroy("counter").get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void fetch() throws URISyntaxException, ExecutionException, InterruptedException, ProtocolClientException {
        when(clientFactory.getClient()).thenReturn(client);
        when(client.readResource(any())).thenReturn(completedFuture(null));

        Servient servient = new Servient(List.of(), Map.of("test", clientFactory), Map.of(), Map.of());
        servient.fetch("test:/counter").get();

        verify(client).readResource(any());
    }

    @Test
    public void fetchMissingScheme() throws Throwable {
        Servient servient = new Servient(List.of(), Map.of(), Map.of(), Map.of());

        assertThrows(ServientException.class, () -> {
            try {
                servient.fetch("test:/counter").get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void fetchDirectoryShouldCallUnderlyingClient() throws URISyntaxException, ExecutionException, InterruptedException, ProtocolClientException {
        when(clientFactory.getClient()).thenReturn(client);
        when(client.readResource(any())).thenReturn(completedFuture(null));

        Servient servient = new Servient(List.of(), Map.of("test", clientFactory), Map.of(), Map.of());
        servient.fetchDirectory(new URI("test:/")).get();

        verify(client).readResource(any());
    }

    @Test
    public void fetchDirectoryWithStringShouldCallUnderlyingClient() throws URISyntaxException, ExecutionException, InterruptedException, ProtocolClientException {
        when(clientFactory.getClient()).thenReturn(client);
        when(client.readResource(any())).thenReturn(completedFuture(null));

        Servient servient = new Servient(List.of(), Map.of("test", clientFactory), Map.of(), Map.of());
        servient.fetchDirectory("test:/").get();

        verify(client).readResource(any());
    }

    @Test
    public void discoverShouldCallUnderlyingClient() throws ServientException {
        when(clientFactory.getClient()).thenReturn(client);
        when(client.discover(any())).thenReturn(Observable.just(thing));

        Servient servient = new Servient(List.of(), Map.of("test", clientFactory), Map.of(), Map.of());
        servient.discover();

        verify(client).discover(any());
    }

    @Test
    public void discoverWithQueryShouldCallUnderlyingClient() throws ServientException {
        ThingFilter filter = new ThingFilter().setQuery(new SparqlThingQuery("?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://www.w3.org/2019/wot/td##Thing> ."));
        when(clientFactory.getClient()).thenReturn(client);
        when(client.discover(any())).thenReturn(Observable.just(thing));

        Servient servient = new Servient(List.of(), Map.of("test", clientFactory), Map.of(), Map.of());
        servient.discover(filter);

        verify(client).discover(filter);
    }

    @Test
    public void discoverLocalShouldReturnThingsOfTheServient() throws ServientException {
        Servient servient = spy(new Servient(List.of(), Map.of(), Map.of(), Map.of("counter", exposedThing)));
        ThingFilter filter = new ThingFilter().setMethod(DiscoveryMethod.LOCAL);
        @NonNull List<Thing> things = servient.discover(filter).toList().blockingGet();

        verify(servient).getThings();
        assertThat(things, hasItem(exposedThing));
    }

    @Test
    public void discoverDirectoryShouldAccessDirectoryWithUnderlyingClient() throws URISyntaxException, ServientException {
        when(clientFactory.getClient()).thenReturn(client);
        when(client.readResource(any())).thenReturn(completedFuture(null));

        Servient servient = new Servient(List.of(), Map.of("test", clientFactory), Map.of(), Map.of());
        ThingFilter filter = new ThingFilter().setMethod(DiscoveryMethod.DIRECTORY).setUrl(new URI("test:/"));
        servient.discover(filter);

        verify(client).readResource(new Form.Builder().setHref("test:/").build());
    }

    @Test
    public void discoverWithNoClientImplementsDiscoverShouldFail() {
        Servient servient = new Servient(List.of(), Map.of(), Map.of(), Map.of());
        assertThrows(ProtocolClientNotImplementedException.class, () -> servient.discover());
    }

    @Test
    public void discoverWithAtLeastOneClientImplementingDiscoverShouldSucceed() throws Throwable {
        when(exposedThing.getId()).thenReturn("counter");
        when(clientFactory.getClient()).thenReturn(client);
        when(client.discover(any())).thenReturn(Observable.just(exposedThing));

        Servient servient = new Servient(List.of(), Map.of("test", clientFactory), Map.of(), Map.of());
        @NonNull List<Thing> things = servient.discover().toList().blockingGet();

        assertThat(things, hasItem(exposedThing));
    }

    @Test
    public void runScriptWithNoEngine() {
        Servient servient = new Servient(List.of(), Map.of(), Map.of(), Map.of());

        assertThrows(ServientException.class, () -> {
            try {
                servient.runScript(new File("foo.bar"), null).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void runPrivilegedScriptWithNoEngine() {
        Servient servient = new Servient(List.of(), Map.of(), Map.of(), Map.of());

        assertThrows(ServientException.class, () -> {
            try {
                servient.runPrivilegedScript(new File("foo.bar"), null).get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void getAddresses() {
        Servient.getAddresses();

        // should not fail
        assertTrue(true);
    }

    @Test
    public void addThingWithoutId() {
        when(exposedThing.getId()).thenReturn(null);

        Servient servient = new Servient(List.of(), Map.of(), Map.of(), new HashMap<>());
        servient.addThing(exposedThing);

        verify(exposedThing).setId(any());
    }

    @Test
    public void getClientForNegative() throws ProtocolClientException {
        Servient servient = new Servient(List.of(), Map.of(), Map.of(), Map.of());

        assertNull(servient.getClientFor("test"));
    }

    @Test
    public void registerShouldThrowExceptionBecauseNotImplemented() throws Throwable {
        Servient servient = new Servient(List.of(), Map.of(), Map.of(), Map.of());

        assertThrows(ServientException.class, () -> {
            try {
                servient.register("test://foo/bar", null).get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void unregisterShouldThrowExceptionBecauseNotImplemented() throws Throwable {
        Servient servient = new Servient(List.of(), Map.of(), Map.of(), Map.of());

        assertThrows(ServientException.class, () -> {
            try {
                servient.unregister("test://foo/bar", null).get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }
}