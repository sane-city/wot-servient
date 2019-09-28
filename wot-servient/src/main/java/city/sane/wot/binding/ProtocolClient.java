package city.sane.wot.binding;

import city.sane.wot.content.Content;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import city.sane.wot.thing.security.SecurityScheme;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A ProtocolClient defines how to interact with a Thing via a specific protocol (e.g. HTTP, MQTT, etc.).
 */
public interface ProtocolClient {
    /**
     * Reads the resource defined in <code>form</code>. This can be a {@link city.sane.wot.thing.property.ThingProperty}, a {@link Thing} or a Thing Directory.
     *
     * @param form
     *
     * @return
     */
    default CompletableFuture<Content> readResource(Form form) {
        return CompletableFuture.failedFuture(new ProtocolClientNotImplementedException(getClass(), "read"));
    }

    /**
     * Writes <code>content</code> to the resource defined in <code>form</code>. This can be, for example, a {@link city.sane.wot.thing.property.ThingProperty}.
     *
     * @param form
     * @param content
     *
     * @return
     */
    default CompletableFuture<Content> writeResource(Form form, Content content) {
        return CompletableFuture.failedFuture(new ProtocolClientNotImplementedException(getClass(), "write"));
    }

    /**
     * Invokes the resource defined in the <code>form</code> with the payload defined in <code>content</code>. This can be a
     * {@link city.sane.wot.thing.action.ThingAction}, for example.
     *
     * @param form
     * @param content
     *
     * @return
     */
    default CompletableFuture<Content> invokeResource(Form form, Content content) {
        return CompletableFuture.failedFuture(new ProtocolClientNotImplementedException(getClass(), "invoke"));
    }

    /**
     * Invokes the resource defined in the <code>form</code>. This can be a {@link city.sane.wot.thing.action.ThingAction}, for example.
     *
     * @param form
     *
     * @return
     */
    default CompletableFuture<Content> invokeResource(Form form) {
        return invokeResource(form, null);
    }

    /**
     * Subscribes <code>observer</code> to the resource defined in <code>form</code>. This can be, for example, an {@link city.sane.wot.thing.event.ThingEvent}
     * or an observable {@link city.sane.wot.thing.property.ThingProperty}.
     *
     * @param form
     * @param observer
     *
     * @return
     * @throws ProtocolClientNotImplementedException
     */
    default CompletableFuture<Subscription> subscribeResource(Form form, Observer<Content> observer) throws ProtocolClientNotImplementedException {
        throw new ProtocolClientNotImplementedException(getClass(), "subscribe");
    }

    /**
     * Subscribes to the resource defined in <code>form</code>. This can be, for example, an {@link city.sane.wot.thing.event.ThingEvent}
     * or an observable {@link city.sane.wot.thing.property.ThingProperty}.
     * <code>next</code> is called with every new result of the subscription and gets the new result passed to it.
     * <code>error</code> is called if the subscription has to be terminated due to an error. The error is passed to the {@link Consumer}.
     * <code>complete</code> is called when the subscription is complete and there will be no more new results.
     *
     * @param form
     * @param next
     * @param error
     * @param complete
     *
     * @return
     * @throws ProtocolClientNotImplementedException
     */
    default CompletableFuture<Subscription> subscribeResource(Form form,
                                                              Consumer<Content> next,
                                                              Consumer<Throwable> error,
                                                              Runnable complete) throws ProtocolClientNotImplementedException {
        return subscribeResource(form, new Observer<>(next, error, complete));
    }

    /**
     * Adds the <code>metadata</code> with security mechanisms (e.g. use password authentication) and <code>credentials</code>credentials (e.g. password and
     * username) of a things to the client.
     *
     * @param metadata
     * @param credentials
     *
     * @return
     */
    default boolean setSecurity(List<SecurityScheme> metadata, Object credentials) {
        return false;
    }

    /**
     * Starts the discovery process that will provide Things that match the <code>filter</code> argument.
     *
     * @param filter
     *
     * @return
     */
    default CompletableFuture<Collection<Thing>> discover(ThingFilter filter) {
        return CompletableFuture.failedFuture(new ProtocolClientNotImplementedException(getClass(), "discover"));
    }
}
