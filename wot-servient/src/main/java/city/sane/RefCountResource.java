package city.sane;

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

/**
 * This class provides a reference counted resource. At the first access ({@link #retain()} an
 * internal reference counter is incremented and the resource is created and returned using sss. The
 * resource can be released with {@link #release()}. This decrements the internal reference counter.
 * As soon as the counter is zero, the resource is destroyed using xxx.
 */
public class RefCountResource<T> {
    private static final Logger log = LoggerFactory.getLogger(RefCountResource.class);
    private final Supplier<? extends T> resourceSupplier;
    private final Consumer<? super T> resourceCleanup;
    private final AtomicInteger refCount;
    private AtomicReference<T> resource;

    public RefCountResource(Supplier<? extends T> resourceSupplier,
                            Consumer<? super T> resourceCleanup) {
        this(
                resourceSupplier,
                resourceCleanup,
                new AtomicInteger(0),
                new AtomicReference(null)
        );
    }

    RefCountResource(Supplier<? extends T> resourceSupplier,
                     Consumer<? super T> resourceCleanup,
                     AtomicInteger refCount,
                     AtomicReference<T> resource) {
        this.resourceSupplier = requireNonNull(resourceSupplier);
        this.resourceCleanup = requireNonNull(resourceCleanup);
        this.refCount = requireNonNull(refCount);
        this.resource = requireNonNull(resource);
    }

    /**
     * Increases the reference count by {@code 1}, ensures the resource is created and returns it.
     */
    public T retain() throws RefCountResourceException {
        synchronized (this) {
            if (refCount.getAndIncrement() == 0) {
                try {
                    log.debug("First Reference. Create Resource.");
                    resource.set(resourceSupplier.get());
                }
                catch (Throwable throwable) {
                    throw new RefCountResourceException(throwable);
                }
            }

            return resource.get();
        }
    }

    /**
     * Decreases the reference count by {@code 1} and deallocates the resource if the reference
     * count reaches at {@code 0}.
     */
    public void release() throws RefCountResourceException {
        synchronized (this) {
            if (refCount.updateAndGet(i -> i > 0 ? i - 1 : i) == 0 && resource.get() != null) {
                log.debug("No more References. Cleanup Resource: {}", resource.get());
                try {
                    resourceCleanup.accept(resource.getAndSet(null));
                }
                catch (Throwable throwable) {
                    throw new RefCountResourceException(throwable);
                }
            }
        }
    }
}
