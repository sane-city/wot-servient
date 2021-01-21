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
package city.sane.wot.binding;

import city.sane.wot.content.Content;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.form.Form;
import io.reactivex.rxjava3.core.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
public class ProtocolClientTest {
    private Form form;
    private Content content;
    private Observer observer;
    private List metadata;
    private Object credentials;
    private ThingFilter filter;
    private ProtocolClient client;

    @BeforeEach
    public void setUp() {
        form = mock(Form.class);
        content = mock(Content.class);
        observer = mock(Observer.class);
        metadata = mock(List.class);
        credentials = mock(Object.class);
        filter = mock(ThingFilter.class);
        client = spy(ProtocolClient.class);
    }

    @Test
    public void readResourceShouldThrowProtocolClientNotImplementedException() {
        assertThrows(ProtocolClientNotImplementedException.class, () -> {
            try {
                client.readResource(form).get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void writeResourceShouldThrowProtocolClientNotImplementedException() {
        assertThrows(ProtocolClientNotImplementedException.class, () -> {
            try {
                client.writeResource(form, content).get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void invokeResourceShouldThrowProtocolClientNotImplementedException() {
        assertThrows(ProtocolClientNotImplementedException.class, () -> {
            try {
                client.invokeResource(form).get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void subscribeResourceShouldThrowProtocolClientNotImplementedException() {
        assertThrows(ProtocolClientNotImplementedException.class, () -> client.observeResource(form).subscribe(observer));
    }

    @Test
    public void setSecurityShouldReturnFalse() {
        assertFalse(client.setSecurity(metadata, credentials));
    }

    @Test
    public void discoverShouldThrowProtocolClientNotImplementedException() {
        assertThrows(ProtocolClientNotImplementedException.class, () -> client.discover(filter));
    }
}
