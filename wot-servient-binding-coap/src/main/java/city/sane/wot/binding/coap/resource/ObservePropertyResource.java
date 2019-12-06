package city.sane.wot.binding.coap.resource;

import city.sane.wot.thing.property.ExposedThingProperty;

/**
 * Endpoint for subscribing to value changes for a {@link city.sane.wot.thing.property.ThingProperty}.
 */
public class ObservePropertyResource extends AbstractSubscriptionResource {
    public ObservePropertyResource(String name, ExposedThingProperty property) {
        super("observable", name, property);
    }
}
