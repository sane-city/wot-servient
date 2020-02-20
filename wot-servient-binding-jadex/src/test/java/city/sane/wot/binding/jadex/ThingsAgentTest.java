package city.sane.wot.binding.jadex;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.IntegerSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.commons.future.IFunctionalResultListener;
import jadex.commons.future.IFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ThingsAgentTest {
    private ExposedThing thing;
    @Mock
    private IInternalAccess ia;
    private ThingsAgent agent;
    private Map<String, IExternalAccess> children;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(ia.createComponent(anyObject())).thenReturn(mock(IFuture.class));

        thing = getExposedCounterThing();
        Map<String, ExposedThing> things = Map.of("counter", thing);
        children = new HashMap<>();
        agent = new ThingsAgent(ia, things, children);
    }

    private ExposedThing getExposedCounterThing() {
        ThingProperty counterProperty = new ThingProperty.Builder()
                .setType("integer")
                .setDescription("current counter content")
                .setObservable(true)
                .build();

        ThingProperty lastChangeProperty = new ThingProperty.Builder()
                .setType("string")
                .setDescription("last change of counter content")
                .setObservable(true)
                .setReadOnly(true)
                .build();

        ExposedThing thing = new ExposedThing(null)
                .setId("counter")
                .setTitle("counter");

        thing.addProperty("count", counterProperty, 42);
        thing.addProperty("lastChange", lastChangeProperty, new Date().toString());

        thing.addAction("increment",
                new ThingAction.Builder()
                        .setDescription("Incrementing counter content with optional step content as uriVariable")
                        .setUriVariables(Map.of(
                                "step", Map.of(
                                        "type", "integer",
                                        "minimum", 1,
                                        "maximum", 250
                                )
                        ))
                        .setInput(new ObjectSchema())
                        .setOutput(new IntegerSchema())
                        .build(),
                (input, options) -> {
                    return thing.getProperty("count").read().thenApply(value -> {
                        int step;
                        if (input != null && ((Map) input).containsKey("step")) {
                            step = (Integer) ((Map) input).get("step");
                        }
                        else if (options.containsKey("uriVariables") && ((Map) options.get("uriVariables")).containsKey("step")) {
                            step = (int) ((Map) options.get("uriVariables")).get("step");
                        }
                        else {
                            step = 1;
                        }
                        int newValue = ((Integer) value) + step;
                        thing.getProperty("count").write(newValue);
                        thing.getProperty("lastChange").write(new Date().toString());
                        thing.getEvent("change").emit();
                        return newValue;
                    });
                });

        thing.addAction("decrement", new ThingAction<Object, Object>(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int newValue = ((Integer) value) - 1;
                thing.getProperty("count").write(newValue);
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return newValue;
            });
        });

        thing.addAction("reset", new ThingAction<Object, Object>(), (input, options) -> {
            return thing.getProperty("count").write(0).thenApply(value -> {
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return 0;
            });
        });

        thing.addEvent("change", new ThingEvent<Object>());

        return thing;
    }

    @Test
    public void created() {
        agent.created().get();

        // shot not fail
        assertTrue(true);
    }

    @Test
    public void expose() {
        agent.expose("counter").get();

        // CreateionInfo ist not comparable
//        CreationInfo info = new CreationInfo()
//                .setFilenameClass(ThingAgent.class)
//                .addArgument("thing", thing);
        verify(ia).createComponent(anyObject());
    }

    @Test
    public void destroy() {
        IExternalAccess ea = mock(IExternalAccess.class);
        IFuture killFuture = mock(IFuture.class);
        doAnswer(invocation -> {
            IFunctionalResultListener listener = invocation.getArgument(0, IFunctionalResultListener.class);
            listener.resultAvailable(null);
            return null;
        }).when(killFuture).addResultListener(anyObject(), anyObject());
        when(ea.killComponent()).thenReturn(killFuture);
        children.put("counter", ea);

        agent.destroy("counter").get();

        verify(ea).killComponent();
    }
}