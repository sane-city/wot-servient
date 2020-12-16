package city.sane.wot.thing.event;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExposedThingEventTest {
    private String name;
    private EventState state;
    private Subject subject;
    private Observer observer;

    @BeforeEach
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