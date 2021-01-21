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
package city.sane.wot.thing.action;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * This class represented the container for the handler of a {@link ThingAction}. The handler is
 * executed when the action is invoked.
 */
public class ActionState<I, O> {
    private BiFunction<I, Map<String, Map<String, Object>>, CompletableFuture<O>> handler;

    public ActionState() {
        this(null);
    }

    ActionState(BiFunction<I, Map<String, Map<String, Object>>, CompletableFuture<O>> handler) {
        this.handler = handler;
    }

    public BiFunction<I, Map<String, Map<String, Object>>, CompletableFuture<O>> getHandler() {
        return handler;
    }

    public void setHandler(BiFunction<I, Map<String, Map<String, Object>>, CompletableFuture<O>> handler) {
        this.handler = handler;
    }
}
