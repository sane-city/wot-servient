package city.sane;

import io.reactivex.rxjava3.core.Observable;

import java.util.concurrent.CompletableFuture;

public class Futures {
    private Futures() {
        // factory class
    }

    public static <T> Observable<T> toObservable(CompletableFuture<T> future) {
        return Observable.create(source -> {
            future.whenComplete((result, e) -> {
                if (e == null) {
                    source.onNext(result);
                    source.onComplete();
                }
                else {
                    source.onError(e);
                }
            });
        });
    }
}
