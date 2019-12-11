package city.sane.wot.thing.event;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ConsumedThingException;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Used in combination with {@link ConsumedThing} and allows consuming of a {@link ThingEvent}.
 */
public class ConsumedThingEvent extends ThingEvent {
    private static final Logger log = LoggerFactory.getLogger(ConsumedThingEvent.class);

    private final String name;
    private final ConsumedThing thing;

    public ConsumedThingEvent(String name, ThingEvent event, ConsumedThing thing) {
        this.name = name;
        forms = event.getForms();
        type = event.getType();
        data = event.getData();
        this.thing = thing;
    }

    private CompletableFuture<Subscription> subscribe(Observer<Object> observer) throws ConsumedThingException {
        Pair<ProtocolClient, Form> clientAndForm = thing.getClientFor(getForms(), Operation.SUBSCRIBE_EVENT);
        ProtocolClient client = clientAndForm.first();
        Form form = clientAndForm.second();

        log.debug("New subscription for Event '{}' from '{}'", name, thing.getTitle());
        try {
            return client.subscribeResource(form,
                    new Observer<>(content -> {
                        try {
                            Object value = ContentManager.contentToValue(content, getData());
                            observer.next(value);
                        }
                        catch (ContentCodecException e) {
                            observer.error(e);
                        }
                    }, observer::next, observer::complete));
        }
        catch (ProtocolClientException e) {
            throw new ConsumedThingException(e);
        }
    }

    public CompletableFuture<Subscription> subscribe(Consumer<Object> next, Consumer<Throwable> error, Runnable complete) throws ConsumedThingException {
        return subscribe(new Observer<>(next, error, complete));
    }

    public CompletableFuture<Subscription> subscribe(Consumer<Object> next) throws ConsumedThingException {
        return subscribe(new Observer<>(next));
    }
}
