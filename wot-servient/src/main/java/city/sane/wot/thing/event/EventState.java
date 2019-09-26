package city.sane.wot.thing.event;

import city.sane.wot.thing.observer.Subject;

/**
 * This class represented the container for the subject of a {@link ThingEvent}. The subject is used as a resource for new values and observers of the event.
 */
public class EventState {
    private final Subject subject;

    public EventState() {
        this.subject = new Subject();
    }

    public Subject getSubject() {
        return subject;
    }
}
