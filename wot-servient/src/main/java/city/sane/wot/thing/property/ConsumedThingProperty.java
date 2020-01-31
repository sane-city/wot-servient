package city.sane.wot.thing.property;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Used in combination with {@link ConsumedThing} and allows consuming of a {@link ThingProperty}.
 */
public class ConsumedThingProperty extends ThingProperty {
    private static final Logger log = LoggerFactory.getLogger(ConsumedThingProperty.class);

    private final String name;
    private final ConsumedThing thing;

    public ConsumedThingProperty(String name, ThingProperty property, ConsumedThing thing) {
        this.name = name;

        objectType = property.getObjectType();
        description = property.getDescription();
        type = property.getType();
        observable = property.isObservable();
        readOnly = property.isReadOnly();
        writeOnly = property.isWriteOnly();
        forms = normalizeHrefs(property.getForms(), thing);
        uriVariables = property.getUriVariables();
        optionalProperties = property.getOptionalProperties();

        this.thing = thing;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private List<Form> normalizeHrefs(List<Form> forms, ConsumedThing thing) {
        return forms.stream().map(f -> normalizeHref(f, thing)).collect(Collectors.toList());
    }

    private Form normalizeHref(Form form, ConsumedThing thing) {
        String base = thing.getBase();
        if (base != null && !base.isEmpty() && !form.getHref().matches("^(?i:[a-z+]+:).*")) {
            String normalizedHref = base + form.getHref();
            return new Form.Builder(form).setHref(normalizedHref).build();
        }
        else {
            return form;
        }
    }

    public CompletableFuture<Object> read() {
        try {
            Pair<ProtocolClient, Form> clientAndForm = thing.getClientFor(getForms(), Operation.READ_PROPERTY);
            ProtocolClient client = clientAndForm.first();
            Form form = clientAndForm.second();

            log.debug("Thing '{}' reading Property '{}' from '{}'", thing.getTitle(), name, form.getHref());

            CompletableFuture<Content> result = client.readResource(form);
            return result.thenApply(content -> {
                try {
                    return ContentManager.contentToValue(content, this);
                }
                catch (ContentCodecException e) {
                    throw new CompletionException(new ConsumedThingException("Received invalid writeResource from Thing: " + e.getMessage()));
                }
            });
        }
        catch (ConsumedThingException e) {
            throw new CompletionException(e);
        }
    }

    public CompletableFuture<Object> write(Object value) {
        try {
            Pair<ProtocolClient, Form> clientAndForm = thing.getClientFor(getForms(), Operation.WRITE_PROPERTY);
            ProtocolClient client = clientAndForm.first();
            Form form = clientAndForm.second();

            log.debug("ConsumedThing {} writing {}", thing.getTitle(), form.getHref());

            Content input = ContentManager.valueToContent(value, form.getContentType());

            CompletableFuture<Content> result = client.writeResource(form, input);
            return result.thenApply(content -> {
                try {
                    return ContentManager.contentToValue(content, this);
                }
                catch (ContentCodecException e) {
                    throw new CompletionException(new ConsumedThingException("Received invalid writeResource from Thing: " + e.getMessage()));
                }
            });
        }
        catch (ContentCodecException e) {
            throw new CompletionException(new ConsumedThingException("Received invalid input: " + e.getMessage()));
        }
        catch (ConsumedThingException e) {
            throw new CompletionException(e);
        }
    }

    private CompletableFuture<Subscription> subscribe(Observer<Object> observer) throws ConsumedThingException {
        Pair<ProtocolClient, Form> clientAndForm = thing.getClientFor(getForms(), Operation.OBSERVE_PROPERTY);
        ProtocolClient client = clientAndForm.first();
        Form form = clientAndForm.second();

        log.debug("New subscription for '{}'", thing.getTitle());
        try {
            return client.subscribeResource(form,
                    content -> {
                        try {
                            Object value = ContentManager.contentToValue(content, this);
                            observer.next(value);
                        }
                        catch (ContentCodecException e) {
                            observer.error(e);
                        }
                    },
                    observer::error, observer::complete);
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
