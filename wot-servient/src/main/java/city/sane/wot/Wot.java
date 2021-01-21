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
package city.sane.wot;

import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.ThingFilter;
import io.reactivex.rxjava3.core.Observable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

/**
 * Provides methods for discovering, consuming, exposing and fetching things.
 * https://w3c.github.io/wot-scripting-api/#the-wot-api-object
 */
public interface Wot {
    /**
     * Starts the discovery process that will provide Things that match the <code>filter</code>
     * argument.
     *
     * @param filter
     * @return
     */
    Observable<Thing> discover(ThingFilter filter) throws WotException;

    /**
     * Starts the discovery process that will provide all available Things.
     *
     * @return
     */
    Observable<Thing> discover() throws WotException;

    /**
     * Accepts a <code>thing</code> argument of type {@link Thing} and returns an {@link
     * ExposedThing} object.<br> The result can be used to start exposing interfaces for thing
     * interaction. Returns a failed future if thing with same id is already exposed.
     *
     * @param thing
     * @return
     * @throws WotException If thing with same id is already exposed
     */
    ExposedThing produce(Thing thing) throws WotException;

    /**
     * Accepts a <code>thing</code> argument of type {@link Thing} and returns a {@link
     * ConsumedThing} object.<br> The result can be used to interact with a thing.
     *
     * @param thing
     * @return
     */
    ConsumedThing consume(Thing thing);

    /**
     * Accepts the <a href="https://www.w3.org/TR/wot-thing-description/">Thing Description</a> in
     * <code>thing</code> and returns a {@link ConsumedThing} object.<br> The result can be used to
     * interact with a thing.
     *
     * @param thing
     * @return
     */
    ConsumedThing consume(String thing);

    /**
     * Accepts an {@link java.net.URL} (e.g. "file:..." or "http://...") to a resource that serves a
     * thing description and returns the corresponding Thing object.
     *
     * @param url
     * @return
     */
    CompletableFuture<Thing> fetch(URI url);

    /**
     * Accepts an {@link String} containing an url (e.g. "file:..." or "http://...") to a resource
     * that serves a thing description and returns the corresponding Thing object.
     *
     * @param url
     * @return
     */
    CompletableFuture<Thing> fetch(String url) throws URISyntaxException;

    /**
     * Shut down the serving and stop exposing all things
     *
     * @return
     */
    CompletableFuture<Void> destroy();
}
