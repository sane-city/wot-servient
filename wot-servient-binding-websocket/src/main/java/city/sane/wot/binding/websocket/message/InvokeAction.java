package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import org.java_websocket.WebSocket;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class InvokeAction extends ThingInteraction {
    private Content value;

    private InvokeAction() {
        super();
        this.value = null;
    }

    public InvokeAction(String thingId, String name, Content value) {
        super(thingId, name);
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public CompletableFuture<AbstractServerMessage> reply(WebSocket socket, Map<String, ExposedThing> things) {
        String id = getThingId();
        ExposedThing thing = things.get(id);

        if (thing != null) {
            String name = getName();
            ExposedThingAction action = thing.getAction(name);

            if (action != null) {
                Content payload = getValue();

                try {
                    Object input = ContentManager.contentToValue(payload, action.getInput());

                    return action.invoke(input).thenApply(output -> {
                        try {
                            return new InvokeActionResponse(getId(), ContentManager.valueToContent(output));
                        }
                        catch (ContentCodecException e) {
                            throw new CompletionException(e);
                        }
                    });
                } catch (ContentCodecException e) {
                    // unable to parse paylod
                    // FIXME: send 500er error back and remove throw
                    return CompletableFuture.failedFuture(null);
                }
            } else {
                // Property not found
                // FIXME: send 400er message back
                return CompletableFuture.failedFuture(null);
            }
        } else {
            // Thing not found
            // FIXME: send 400er message back
            return CompletableFuture.failedFuture(null);
        }
    }

    @Override
    public String toString() {
        return "InvokeAction [" +
                "thingId='" + thingId + '\'' +
                ", name='" + name + '\'' +
                ']';
    }

    public void setValue(Content value) {
        this.value = value;
    }

    public Content getValue() {
        return value;
    }
}

