package city.sane.wot.binding.akka;

import akka.actor.ActorSystem;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.Future;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

public class SharedActorSystemProviderTest {
    private Supplier<ActorSystem> supplier;
    private ActorSystem system;

    @Before
    public void setUp() {
        supplier = mock(Supplier.class);
        system = mock(ActorSystem.class);
    }

    @Test
    public void createShouldCreateAnActorSystemOnFirstCall() {
        SharedActorSystemProvider provider = new SharedActorSystemProvider(supplier, new AtomicInteger(0), null, null);
        provider.create();

        verify(supplier, times(1)).get();
    }

    @Test
    public void createShouldNotCreateAnActorSystemOnSecondCall() {
        SharedActorSystemProvider provider = new SharedActorSystemProvider(supplier, new AtomicInteger(1), null, null);
        provider.create();

        verify(supplier, times(0)).get();
    }

    @Test
    public void terminateShouldTerminateActorSystemWhenItIsNoLongerUsed() {
        when(system.terminate()).thenReturn(mock(Future.class));

        SharedActorSystemProvider provider = new SharedActorSystemProvider(supplier, new AtomicInteger(1), system, null);
        provider.terminate();

        verify(system, times(1)).terminate();
    }

    @Test
    public void terminateShouldNotTerminateActorSystemWhenItIsStillUsed() {
        SharedActorSystemProvider provider = new SharedActorSystemProvider(supplier, new AtomicInteger(2), system, null);
        provider.terminate();

        verify(system, times(0)).terminate();
    }

    @Test
    public void terminateShouldDoNothingWhenNoActorSystemHasBeenCreated() {
        SharedActorSystemProvider provider = new SharedActorSystemProvider(supplier, new AtomicInteger(0), null, null);
        provider.terminate();

        verify(system, times(0)).terminate();
    }
}