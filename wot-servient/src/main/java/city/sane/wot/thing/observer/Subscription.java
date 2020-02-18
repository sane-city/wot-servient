package city.sane.wot.thing.observer;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Defines a subscription to a resource (for example, a {@link Subject}). This class is part of the
 * Observer pattern (https://en.wikipedia.org/wiki/Observer_pattern)
 */
public class Subscription {
    private final Consumer<Subscription> unsubscribe;
    private final Set<Subscription> subscriptions = new HashSet<>();
    private boolean closed = false;

    /**
     * Creates a new subscription. <code>unsubscribe</code> is called and consumes the current
     * subscription object when the subscription is cancelled (e.g. to remove from a {@link
     * Subject}).
     *
     * @param unsubscribe
     */
    public Subscription(Runnable unsubscribe) {
        this(s -> unsubscribe.run());
    }

    /**
     * Creates a new subscription. <code>unsubscribe</code> is called when the subscription is
     * cancelled (e.g. to remove from a {@link Subject}).
     *
     * @param unsubscribe
     */
    public Subscription(Consumer<Subscription> unsubscribe) {
        this.unsubscribe = unsubscribe;
    }

    /**
     * Creates a new subscription.
     */
    public Subscription() {
        this(s -> {
        });
    }

    /**
     * Ends the subscription.
     */
    public synchronized void unsubscribe() {
        if (!closed) {
            closed = true;
            unsubscribe.accept(this);
            subscriptions.forEach(Subscription::unsubscribe);
        }
    }

    /**
     * Returns <code>true</code> if the subscription was cancelled. Otherwise <code>false</code>.
     *
     * @return
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Creates a new subscription to this subscription. <code>unsubscribe</code> will be called
     * after {@link #unsubscribe()} of this subscription.
     *
     * @param unsubscribe
     * @return
     */
    public Subscription add(Runnable unsubscribe) {
        Subscription subscription = new Subscription(unsubscribe);
        subscriptions.add(subscription);
        return subscription;
    }

    public void remove(Subscription subscription) {
        subscriptions.remove(subscription);
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "subscriptions=" + subscriptions +
                ", closed=" + closed +
                '}';
    }
}
