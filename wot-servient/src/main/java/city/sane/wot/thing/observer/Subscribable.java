package city.sane.wot.thing.observer;

import java.util.function.Consumer;

public interface Subscribable<T> {
    Subscription subscribe(Observer<T> observer);

    /**
     * Creates a new subscription for this subscribable object.<br>
     * <code>next</code> is called with every new result of the subscription and gets the new result passed to it.<br>
     * <code>error</code> is called if the subscription has to be terminated due to an error. The error is passed to the {@link Consumer}.<br>
     * <code>complete</code> is called when the subscription is complete and there will be no more new results.
     *
     * @return
     */
    default Subscription subscribe(Consumer<T> next, Consumer<Throwable> error, Runnable complete) {
        return subscribe(new Observer<>(next, error, complete));
    }

    /**
     * Creates a new subscription for this subscribable object.<br>
     * <code>next</code> is called with every new result of the subscription and gets the new result passed to it.
     *
     * @return
     */
    default Subscription subscribe(Consumer<T> next)  {
        return subscribe(new Observer<>(next));
    }
}
