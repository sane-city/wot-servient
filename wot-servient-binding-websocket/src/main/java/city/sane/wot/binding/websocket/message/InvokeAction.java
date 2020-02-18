package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;

import java.util.Map;
import java.util.function.Consumer;

public class InvokeAction extends ThingInteractionWithContent {
    private InvokeAction() {
        super();
    }

    public InvokeAction(String thingId, String name, Content content) {
        super(thingId, name, content);
    }

    @Override
    public void reply(Consumer<AbstractServerMessage> replyConsumer,
                      Map<String, ExposedThing> things) {
        String id = getThingId();
        ExposedThing thing = things.get(id);

        if (thing != null) {
            String name = getName();
            ExposedThingAction<Object, Object> action = thing.getAction(name);

            if (action != null) {
                Content payload = getValue();

                try {
                    Object input = ContentManager.contentToValue(payload, action.getInput());

                    action.invoke(input).thenAccept(output -> {
                        try {
                            replyConsumer.accept(new InvokeActionResponse(getId(), ContentManager.valueToContent(output)));
                        }
                        catch (ContentCodecException e) {
                            replyConsumer.accept(new ServerErrorResponse(this, "Unable to parse output of invoke operation: " + e.getMessage()));
                        }
                    });
                }
                catch (ContentCodecException e) {
                    replyConsumer.accept(new ServerErrorResponse(this, "Unable to parse input of invoke operation: " + e.getMessage()));
                }
            }
            else {
                // Action not found
                replyConsumer.accept(new ClientErrorResponse(this, "Action not found"));
            }
        }
        else {
            // Thing not found
            replyConsumer.accept(new ClientErrorResponse(this, "Thing not found"));
        }
    }

    @Override
    public String toString() {
        return "InvokeAction{" +
                "value=" + value +
                ", thingId='" + thingId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

