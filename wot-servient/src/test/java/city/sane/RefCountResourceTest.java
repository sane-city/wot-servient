package city.sane;

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Supplier;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class RefCountResourceTest {
    private Supplier resourceSupplier;
    private Consumer resourceCleanup;

    @Before
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