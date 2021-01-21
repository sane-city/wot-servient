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
package city.sane;

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RefCountResourceTest {
    private Supplier resourceSupplier;
    private Consumer resourceCleanup;

    @BeforeEach
    public void setUp() {
        resourceSupplier = mock(Supplier.class);
        resourceCleanup = mock(Consumer.class);
    }

    @Test
    public void retainShouldCreateCreateResourceOnFirstAccess() throws Throwable {
        AtomicInteger refCount = new AtomicInteger(0);
        RefCountResource resource = new RefCountResource(resourceSupplier, resourceCleanup, refCount, new AtomicReference(null));
        resource.retain();

        verify(resourceSupplier).get();
        assertEquals(1, refCount.get());
    }

    @Test
    public void retainShouldNotCreateResourceOnSecondCall() throws Throwable {
        AtomicInteger refCount = new AtomicInteger(1);
        RefCountResource resource = new RefCountResource(resourceSupplier, resourceCleanup, refCount, new AtomicReference(new Object()));
        resource.retain();

        verify(resourceSupplier, times(0)).get();
        assertEquals(2, refCount.get());
    }

    @Test
    public void releaseShouldCleanupResourceWhenItIsNoLongerUsed() throws Throwable {
        AtomicInteger refCount = new AtomicInteger(1);
        RefCountResource resource = new RefCountResource(resourceSupplier, resourceCleanup, refCount, new AtomicReference(new Object()));
        resource.release();

        verify(resourceCleanup).accept(any());
        assertEquals(0, refCount.get());
    }

    @Test
    public void releaseShouldNotCleanupResourceWhenItIsStillUsed() throws Throwable {
        AtomicInteger refCount = new AtomicInteger(2);
        RefCountResource resource = new RefCountResource(resourceSupplier, resourceCleanup, refCount, new AtomicReference(new Object()));
        resource.release();

        verify(resourceCleanup, times(0)).accept(any());
        assertEquals(1, refCount.get());
    }

    @Test
    public void releaseShouldNotCleanupResourceNonCreatedResource() throws Throwable {
        AtomicInteger refCount = new AtomicInteger(0);
        RefCountResource resource = new RefCountResource(resourceSupplier, resourceCleanup, refCount, new AtomicReference(null));
        resource.release();

        verify(resourceCleanup, times(0)).accept(any());
        assertEquals(0, refCount.get());
    }
}