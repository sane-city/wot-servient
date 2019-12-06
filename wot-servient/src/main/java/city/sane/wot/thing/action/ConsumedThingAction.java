package city.sane.wot.thing.action;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ConsumedThingException;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Used in combination with {@link ConsumedThing} and allows consuming of a {@link ThingAction}.
 */
public class ConsumedThingAction extends ThingAction {
    static final Logger log = LoggerFactory.getLogger(ConsumedThingAction.class);

    private final String name;
    private final ConsumedThing thing;

    public ConsumedThingAction(String name, ThingAction action, ConsumedThing thing) {
        this.name = name;
        forms = action.getForms();
        input = action.getInput();
        output = action.getOutput();
        this.thing = thing;
    }

    public CompletableFuture invoke(Map<String, Object> parameters) {
        try {
            Pair<ProtocolClient, Form> clientAndForm = thing.getClientFor(getForms(), Operation.INVOKE_ACTION);
            ProtocolClient client = clientAndForm.first();
            Form form = clientAndForm.second();

            log.debug("Thing '{}' invoking Action '{}' with form '{}' and parameters '{}'", thing.getTitle(), name, form.getHref(), parameters);

            Content input = null;
            if (!parameters.isEmpty()) {
                input = ContentManager.valueToContent(parameters, form.getContentType());
            }

            form = ConsumedThing.handleUriVariables(form, parameters);

            CompletableFuture<Content> result = client.invokeResource(form, input);
            return result.thenApply(content -> {
                try {
                    return ContentManager.contentToValue(content, getOutput());
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

    public CompletableFuture invoke() {
        return invoke(Collections.emptyMap());
    }
}
