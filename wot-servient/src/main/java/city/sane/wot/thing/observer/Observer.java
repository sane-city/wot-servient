package city.sane.wot.thing.observer;


import java.util.function.Consumer;

/**
 * Defines an observer who is informed about new results of a certain resource (e.g. a {@link Subject}).
 * This class is part of the Observer pattern (https://en.wikipedia.org/wiki/Observer_pattern)
 *
 * @param <T>
 */
public class Observer<T> {
    private final Consumer<T> next;
    private final Consumer<Throwable> error;
    private final Runnable complete;

    /**
     * Creates a new observer.<br>
     * <code>next</code> is called on every new observed value and gets the new value passed to it.<br>
     * <code>error</code> is called if the observed resource has to be terminated due to an error. The error is passed to the consumer.<br>
     * <code>complete</code> is called when the observed resource is complete and there will be no more new results.
     *
     * @param next
     * @param error
     * @param complete
     */
    public Observer(Consumer<T> next, Consumer<Throwable> error, Runnable complete) {
        this.next = next;
        this.error = error;
        this.complete = complete;
    }

    /**
     * Creates a new observer.
     * <code>next</code> is called on every new observed value and gets the new value passed to it.
     *
     * @param next
     */
    public Observer(Consumer<T> next) {
        this(next, e -> {
            e.printStackTrace();
        }, () -> {
        });
    }

    /**
     * Informs the Observer about a new <code>value</code>.
     *
     * @param value
     */
    public void next(T value) {
        next.accept(value);
    }

    /**
     * Notifies the Observer of an error in the observed resource.
     *
     * @param e
     */
    public void error(Throwable e) {
        error.accept(e);
    }

    /**
     * Notifies the Observer of the completion of the observed resource
     */
    public void complete() {
        complete.run();
    }
}
