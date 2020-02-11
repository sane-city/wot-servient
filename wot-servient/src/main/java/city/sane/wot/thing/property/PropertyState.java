package city.sane.wot.thing.property;

import city.sane.wot.thing.observer.Subject;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class represented the container for the read and write handlers of a {@link ThingProperty}.
 * The handlers are executed when the property is read or written.
 */
public class PropertyState {
    private final Subject subject;
    private Object value;
    private Supplier readHandler;
    private Function<Object, CompletableFuture<Object>> writeHandler;

    public PropertyState() {
        this(null);
    }

    private PropertyState(Object value) {
        this.subject = new Subject();
        this.value = value;
    }

    public Subject getSubject() {
        return subject;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Supplier<CompletableFuture<Object>> getReadHandler() {
        return readHandler;
    }

    public void setReadHandler(Supplier<CompletableFuture<Object>> readHandler) {
        this.readHandler = readHandler;
    }

    public Function<Object, CompletableFuture<Object>> getWriteHandler() {
        return writeHandler;
    }

    public void setWriteHandler(Function<Object, CompletableFuture<Object>> writeHandler) {
        this.writeHandler = writeHandler;
    }
}
