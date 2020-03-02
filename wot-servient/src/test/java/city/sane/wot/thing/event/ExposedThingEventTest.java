package city.sane.wot.thing.event;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.Subject;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class ExposedThingEventTest {
    private String name;
    private EventState state;
    private Subject subject;
    private Observer observer;

    @Before
    public void setUp() {
        name = "change";
        state = mock(EventState.class);
        subject = mock(Subject.class);
        observer = mock(Observer.class);
    }

    @Test
    public void emitWithoutDataShouldEmitNullAsNextValueToEventState() {
        when(state.getSubject()).thenReturn(subject);

        ExposedThingEvent event = new ExposedThingEvent<>(name, state);
        event.emit();

        verify(subject).onNext(Optional.empty());
    }

    @Test
    public void emitShouldEmitGivenDataAsNextValueToEventState() {
        when(state.getSubject()).thenReturn(subject);

        ExposedThingEvent event = new ExposedThingEvent<>(name, state);
        event.emit("Hallo Welt");

        verify(subject).onNext(Optional.of("Hallo Welt"));
    }

    @Test
    public void subscribeShouldSubscribeToEventState() {
        when(state.getSubject()).thenReturn(subject);

        ExposedThingEvent event = new ExposedThingEvent<>(name, state);
        event.observer().subscribe(observer);

        verify(subject).subscribe(observer);
    }
}