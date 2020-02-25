package city.sane.wot.binding.coap.resource;

import city.sane.wot.thing.event.ExposedThingEvent;

/**
 * Endpoint for interaction with a {@link city.sane.wot.thing.event.ThingEvent}.
 */
public class EventResource extends AbstractSubscriptionResource {
    public EventResource(String name, ExposedThingEvent<Object> event) {
        super(name, name, event.observer());
    }
}
