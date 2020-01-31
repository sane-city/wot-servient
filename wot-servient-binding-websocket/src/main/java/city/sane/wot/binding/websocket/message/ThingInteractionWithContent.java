package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;

import java.util.Objects;

public abstract class ThingInteractionWithContent extends ThingInteraction {
    protected Content value;

    public ThingInteractionWithContent(String thingId, String name, Content value) {
        super(thingId, name);
        this.value = Objects.requireNonNull(value);
    }

    public ThingInteractionWithContent() {
        super();
        this.value = null;
    }

    public Content getValue() {
        return value;
    }

    public void setValue(Content value) {
        this.value = value;
    }
}
