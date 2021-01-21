/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package city.sane.wot.binding.jadex;

import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

import java.util.concurrent.CompletableFuture;

/**
 * Helper class for translating between jadex futures ({@link IFuture}) and java futures ({@link
 * CompletableFuture}).
 */
class FutureConverters {
    private FutureConverters() {
    }

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
