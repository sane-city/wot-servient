package city.sane.wot.binding.jadex;

import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

import java.util.concurrent.CompletableFuture;

/**
 * Helper class for translating between jadex futures ({@link IFuture}) and java futures ({@link CompletableFuture}).
 */
public class FutureConverters {
    public static <T> CompletableFuture<T> fromJadex(IFuture<T> jadexFuture) {
        CompletableFuture<T> future = new CompletableFuture<>();
        jadexFuture.addResultListener(future::complete, future::completeExceptionally);
        return future;
    }

    public static <T> IFuture<T> toJadex(CompletableFuture<T> future) {
        Future<T> jadexFuture = new Future<>();
        future.whenComplete((r, e) -> {
            if (e == null) {
                jadexFuture.setResult(r);
            }
            else {
                if (e instanceof Exception) {
                    jadexFuture.setException((Exception) e);
                }
                else {
                    jadexFuture.setException(new Exception(e));
                }
            }
        });

        return jadexFuture;
    }
}
