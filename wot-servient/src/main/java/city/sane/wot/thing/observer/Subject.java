package city.sane.wot.thing.observer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Defines a subject that can be observed by any number of {@link Observer} instances.
 * This class is part of the Observer pattern (https://en.wikipedia.org/wiki/Observer_pattern)
 *
 * @param <T>
 */
public class Subject<T> implements Subscribable<T> {
    static final Logger log = LoggerFactory.getLogger(Subject.class);
    private final Map<Subscription, Observer> observers = new HashMap<>();
    protected boolean closed = false;

    public Map<Subscription, Observer> getObservers() {
        return observers;
    }

    /**
     * Adds the <code>next</code> value to the subject. Informs all {@link #observers}. Returns a future which will be completed when all observers have been
     * informed.
     *
     * @param value
     *
     * @return
     */
    public CompletableFuture<Void> next(T value) {
        if (closed) {
            return CompletableFuture.failedFuture(new SubjectException("Subject is closed"));
        }

        ArrayList<Observer> observersSnapshot = new ArrayList<>(this.observers.values());
        log.info("Inform {} observer(s) about next value '{}'", observersSnapshot.size(), value);

        // call all observers in parallel
        CompletableFuture[] nextFutures = observersSnapshot.stream()
                .map(o -> CompletableFuture.runAsync(() -> o.next(value))).toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(nextFutures);
    }

    /**
     * Closes the subject with an error. Informs all {@link #observers}. Returns a future which will be completed when all observers have been informed.
     * After that, no more values can be submitted to the subject.
     *
     * @param e
     *
     * @return
     */
    public CompletableFuture<Void> error(Throwable e) {
        if (closed) {
            return CompletableFuture.failedFuture(new SubjectException("Subject is closed"));
        }
        closed = true;

        ArrayList<Observer> observersSnapshot = new ArrayList<>(this.observers.values());
        log.info("Inform {} observer(s) about error '{}'", observersSnapshot.size(), e);

        // call all observers in parallel
        CompletableFuture[] nextFutures = observersSnapshot.stream()
                .map(o -> CompletableFuture.runAsync(() -> o.error(e))).toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(nextFutures);
    }

    /**
     * Complete the subject. Informs all {@link #observers}. Returns a future which will be completed when all observers have been informed.
     * After that, no more values can be submitted to the subject.
     *
     * @return
     */
    public CompletableFuture<Void> complete() {
        if (closed) {
            return CompletableFuture.failedFuture(new SubjectException("Subject is closed"));
        }
        closed = true;

        ArrayList<Observer> observersSnapshot = new ArrayList<>(this.observers.values());
        log.info("Inform {} observer(s) about completion", observersSnapshot.size());

        // call all observers in parallel
        CompletableFuture[] nextFutures = observersSnapshot.stream()
                .map(o -> CompletableFuture.runAsync(o::complete)).toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(nextFutures);
    }

    /**
     * Lets <code>observer</code> subscribe the subject.
     *
     * @param observer
     *
     * @return
     */
    @Override
    public synchronized Subscription subscribe(Observer observer) {
        Subscription subscription = new Subscription(this::unsubscribe);
        observers.put(subscription, observer);
        return subscription;
    }

    /**
     * Removes <code>subscription</code> from the subject.
     *
     * @param subscription
     */
    private synchronized void unsubscribe(Subscription subscription) {
        log.debug("unsubscribe from Subject: {}", subscription);
        observers.remove(subscription);
    }
}
