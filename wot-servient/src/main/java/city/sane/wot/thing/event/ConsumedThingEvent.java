package city.sane.wot.thing.event;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ConsumedThingException;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import io.reactivex.rxjava3.core.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Used in combination with {@link ConsumedThing} and allows consuming of a {@link ThingEvent}.
 */
public class ConsumedThingEvent<T> extends ThingEvent<T> {
    private static final Logger log = LoggerFactory.getLogger(ConsumedThingEvent.class);
    private final String name;
    private final ConsumedThing thing;

    public ConsumedThingEvent(String name, ThingEvent<T> event, ConsumedThing thing) {
        this.name = name;
        forms = event.getForms();
        type = event.getType();
        data = event.getData();
        this.thing = thing;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "ConsumedThingEvent{" +
                "name='" + name + '\'' +
                ", data=" + data +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", descriptions=" + descriptions +
                ", forms=" + forms +
                ", uriVariables=" + uriVariables +
                '}';
    }

    public Observable<Optional<T>> observer() throws ConsumedThingException {
        try {
            Pair<ProtocolClient, Form> clientAndForm = thing.getClientFor(getForms(), Operation.SUBSCRIBE_EVENT);

            ProtocolClient client = clientAndForm.first();
            Form form = clientAndForm.second();

            return client.observeResource(form)
                    .map(content -> Optional.ofNullable(ContentManager.contentToValue(content, getData())));
        }
        catch (ProtocolClientException e) {
            throw new ConsumedThingException(e);
        }
    }
}
