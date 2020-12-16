package city.sane.wot;

import city.sane.wot.thing.Thing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultWotTest {
    private Servient servient;
    private Thing thing;

    @BeforeEach
    public void setUp() {
        servient = mock(Servient.class);
        thing = mock(Thing.class);
    }

    @Test
    public void discoverShouldCallUnderlyingServient() throws WotException {
        DefaultWot wot = new DefaultWot(servient);
        wot.discover();

        verify(servient).discover(any());
    }

    @Test
    public void produceShouldCallUnderlyingServient() throws WotException {
        when(servient.addThing(any())).thenReturn(true);

        DefaultWot wot = new DefaultWot(servient);
        wot.produce(thing);

        verify(servient).addThing(any());
    }

    @Test
    public void consume() {
        DefaultWot wot = new DefaultWot(servient);

        assertNotNull(wot.consume(thing));
    }

    @Test
    public void fetch() throws URISyntaxException {
        DefaultWot wot = new DefaultWot(servient);
        wot.fetch("http://localhost");

        verify(servient).fetch("http://localhost");
    }

    @Test
    public void destroy() {
        DefaultWot wot = new DefaultWot(servient);
        wot.destroy();

        verify(servient).shutdown();
    }

    @Test
    public void clientOnly() throws WotException {
        Wot wot = DefaultWot.clientOnly();

        assertThat(wot, instanceOf(Wot.class));
    }
}