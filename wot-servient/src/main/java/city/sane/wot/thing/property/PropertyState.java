package city.sane.wot.thing.property;

import city.sane.wot.thing.observer.Subject;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * This class represented the container for the read and write handlers of a {@link ThingProperty}.
 * The handlers are executed when the property is read or written.
 */
public class PropertyState<T> {
    private final Subject<T> subject;
    private T value;
    private Supplier<CompletableFuture<T>> readHandler;
    private Function<T, CompletableFuture<Object>> writeHandler;

    public PropertyState() {
        this(new Subject<>(), null, null, null);
    }

    PropertyState(Subject<T> subject,
                  T value,
                  Supplier<CompletableFuture<T>> readHandler,
                  Function<T, CompletableFuture<Object>> writeHandler) {
        this.subject = requireNonNull(subject);
        this.value = value;
        this.readHandler = readHandler;
        this.writeHandler = writeHandler;
    }

    public Subject<T> getSubject() {
        return subject;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Supplier<CompletableFuture<T>> getReadHandler() {
        return readHandler;
    }

    public void setReadHandler(Supplier<CompletableFuture<T>> readHandler) {
        this.readHandler = readHandler;
    }

    public Function<T, CompletableFuture<Object>> getWriteHandler() {
        return writeHandler;
    }

    public void setWriteHandler(Function<T, CompletableFuture<Object>> writeHandler) {
        this.writeHandler = writeHandler;
    }
}
