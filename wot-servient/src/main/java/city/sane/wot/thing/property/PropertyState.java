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
package city.sane.wot.thing.property;

import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * This class represented the container for the read and write handlers of a {@link ThingProperty}.
 * The handlers are executed when the property is read or written.
 */
public class PropertyState<T> {
    private final Subject<Optional<T>> subject;
    private T value;
    private Supplier<CompletableFuture<T>> readHandler;
    private Function<T, CompletableFuture<T>> writeHandler;

    public PropertyState() {
        this(PublishSubject.create(), null, null, null);
    }

    PropertyState(Subject<Optional<T>> subject,
                  T value,
                  Supplier<CompletableFuture<T>> readHandler,
                  Function<T, CompletableFuture<T>> writeHandler) {
        this.subject = requireNonNull(subject);
        this.value = value;
        this.readHandler = readHandler;
        this.writeHandler = writeHandler;
    }

    public Subject<Optional<T>> getSubject() {
        return subject;
    }

    public T getValue() {
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

    public Function<T, CompletableFuture<T>> getWriteHandler() {
        return writeHandler;
    }

    public void setWriteHandler(Function<T, CompletableFuture<T>> writeHandler) {
        this.writeHandler = writeHandler;
    }
}
