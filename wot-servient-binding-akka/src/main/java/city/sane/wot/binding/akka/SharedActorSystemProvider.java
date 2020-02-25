package city.sane.wot.binding.akka;

import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.compat.java8.FutureConverters;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * This class allows different components to use the same ActorSystem. Internally a reference
 * counter is used, which watches the usage of the ActorSystem. As soon as no component uses the
 * ActorSystem anymore, the ActorSystem is terminated. Each call to {@link #create()} increments the
 * counter. Each call to {@link #terminate()} decreases it. If the counter reaches 0 the ActorSystem
 * is terminated
 */
public class SharedActorSystemProvider {
    private static final Logger log = LoggerFactory.getLogger(SharedActorSystemProvider.class);
    private static SharedActorSystemProvider singleton = null;
    private final AtomicInteger referenceCount;
    private final Supplier<ActorSystem> supplier;
    private ActorSystem system;
    private CompletableFuture<Void> terminateFuture;

    private SharedActorSystemProvider(Supplier<ActorSystem> supplier) {
        this(supplier, new AtomicInteger(0), null, null);
    }

    SharedActorSystemProvider(Supplier<ActorSystem> supplier,
                              AtomicInteger referenceCounter,
                              ActorSystem system,
                              CompletableFuture<Void> terminateFuture) {
        this.supplier = requireNonNull(supplier);
        this.referenceCount = requireNonNull(referenceCounter);
        this.system = system;
        this.terminateFuture = terminateFuture;
    }

    /**
     * This method returns an ActorSystem. If no ActorSystem has been created yet, it is created
     * transparently.
     *
     * @return
     */
    public ActorSystem create() {
        synchronized (this) {
            // increment internal reference counter
            int count = referenceCount.getAndIncrement();

            // if not already done, create an ActorSystem asynchronously
            if (count == 0) {
                log.debug("First reference. Create Actor System.");
                terminateFuture = null;
                system = supplier.get();
            }

            return system;
        }
    }

    /**
     * This method terminates the ActorSystem if it is no longer needed by anyone.
     *
     * @return
     */
    public CompletableFuture<Void> terminate() {
        synchronized (this) {
            // decrement internal reference counter
            int count = referenceCount.updateAndGet(i -> i > 0 ? i - 1 : i);

            if (count == 0 && system != null) {
                log.debug("No more references. Terminate ActorSystem.");
                if (terminateFuture == null) {
                    terminateFuture = FutureConverters.toJava(system.terminate())
                            .toCompletableFuture().thenApply(r -> null);
                    system = null;
                }

                return terminateFuture;
            }
            else {
                return completedFuture(null);
            }
        }
    }

    public static synchronized SharedActorSystemProvider singleton(Supplier<ActorSystem> supplier) {
        if (singleton == null) {
            singleton = new SharedActorSystemProvider(supplier);
        }
        return singleton;
    }
}
