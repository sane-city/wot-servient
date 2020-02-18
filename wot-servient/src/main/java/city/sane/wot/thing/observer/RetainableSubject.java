package city.sane.wot.thing.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link Subject} that caches all values and repeats them for new observers.
 *
 * @param <T>
 */
public class RetainableSubject<T> extends Subject<T> {
    private static final Logger log = LoggerFactory.getLogger(RetainableSubject.class);
    // Todo: add size limit to prevent memory leaks
    private final ArrayList<T> storedValues = new ArrayList<>();

    @Override
    public CompletableFuture<Void> next(T value) {
        CompletableFuture<Void> superResult = super.next(value);

        storedValues.add(value);
        return superResult;
    }

    @Override
    public synchronized Subscription subscribe(Observer observer) {
        Subscription subscribe = super.subscribe(observer);
        log.debug("Inform new observer about {} retained value(s)", storedValues.size());
        storedValues.forEach(observer::next);

        return subscribe;
    }
}
