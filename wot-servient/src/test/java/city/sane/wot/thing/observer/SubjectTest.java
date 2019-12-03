package city.sane.wot.thing.observer;

import org.junit.Test;

import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class SubjectTest {
    @Test
    public void next() {
        AtomicReference<String> result = new AtomicReference<>();
        Subject<String> subject = new Subject();
        subject.subscribe(result::set);
        subject.next("Hello World").join();

        assertEquals("Hello World", result.get());
    }

    @Test
    public void error() {
        AtomicReference<Throwable> result = new AtomicReference<>();
        Subject<String> subject = new Subject();
        subject.subscribe(next -> {
        }, e -> result.set(e), () -> {
        });
        subject.error(new Exception("Fatal Error")).join();

        assertEquals(new Exception("Fatal Error").getMessage(), result.get().getMessage());
    }

    @Test
    public void complete() {
        AtomicBoolean completed = new AtomicBoolean();
        Subject<String> subject = new Subject();
        subject.subscribe(next -> {
        }, e -> {
        }, () -> completed.set(true));
        subject.complete().join();

        assertTrue("Subject should be completed", completed.get());
    }

    @Test(expected = CompletionException.class)
    public void nextAfterComplete() {
        Subject subject = new Subject();
        subject.complete().join();
        subject.next(null).join();
    }

    @Test(expected = CompletionException.class)
    public void duplicateError() {
        Subject subject = new Subject();
        subject.error(null).join();
        subject.error(null).join();
    }

    @Test(expected = CompletionException.class)
    public void duplicateComplete() {
        Subject subject = new Subject();
        subject.complete().join();
        subject.complete().join();
    }

    @Test
    public void unsubscribe() {
        Subject subject = new Subject();
        Subscription subscription = subject.subscribe(next -> {
        });

        assertEquals(1, subject.getObservers().size());

        subscription.unsubscribe();
        assertEquals(0, subject.getObservers().size());
    }

    @Test
    public void add() {
        AtomicInteger counter = new AtomicInteger();

        // create parent subs
        Subscription subscription = new Subscription();

        // create child sub
        subscription.add(counter::incrementAndGet);
        subscription.add(counter::incrementAndGet);
        subscription.add(counter::incrementAndGet);

        subscription.unsubscribe();

        assertEquals(3, counter.get());
    }

    @Test
    public void remove() {
        AtomicInteger counter = new AtomicInteger();

        // create parent sub
        Subscription subscription = new Subscription();

        // create child subs
        subscription.add(counter::incrementAndGet);
        subscription.add(counter::incrementAndGet);
        Subscription child = subscription.add(counter::incrementAndGet);

        // remove child sub
        subscription.remove(child);

        subscription.unsubscribe();

        assertEquals(2, counter.get());
    }
}
