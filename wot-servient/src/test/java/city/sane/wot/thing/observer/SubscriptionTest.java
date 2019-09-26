package city.sane.wot.thing.observer;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class SubscriptionTest {
    @Test
    public void unsubscribe() {
        AtomicBoolean executed = new AtomicBoolean();
        Subscription subscription = new Subscription(() -> executed.set(true));
        subscription.unsubscribe();

        assertTrue("Subscription runnable should have been executed",executed.get());
    }
    @Test
    public void unsubscribeOnce() {
        AtomicInteger counter = new AtomicInteger();
        Subscription subscription = new Subscription(() -> counter.incrementAndGet());
        subscription.unsubscribe();
        subscription.unsubscribe();

        assertEquals(1, counter.get());
    }

    @Test
    public void isClosed() {
        AtomicBoolean executed = new AtomicBoolean();
        Subscription subscription = new Subscription(() -> executed.set(true));

        assertFalse("Subscription should not be closed", subscription.isClosed());
        subscription.unsubscribe();
        assertTrue("Subscription should be closed", subscription.isClosed());
    }
}