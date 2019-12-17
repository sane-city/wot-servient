package city.sane.wot.binding.jadex;

import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.IntegerSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.IService;
import jadex.bridge.service.IServiceIdentifier;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ThingAgentTest {
    private ExposedThing thing;
    @Mock
    private IInternalAccess ia;
    private ThingAgent agent;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ThingService thingService = mock(ThingService.class, withSettings().extraInterfaces(IService.class));
        when(((IService) thingService).getServiceId()).thenReturn(mock(IServiceIdentifier.class));
        when(ia.getProvidedService(ThingService.class)).thenReturn(thingService);

        thing = getExposedCounterThing();
        agent = new ThingAgent(ia, thing);
    }

    @Test
    public void created() {
        agent.created().get();

        assertTrue("There must be at least one form", !thing.getProperty("count").getForms().isEmpty());
        assertTrue("There must be at least one action", !thing.getAction("increment").getForms().isEmpty());
        assertTrue("There must be at least one event", !thing.getEvent("change").getForms().isEmpty());
    }

    @Test
    public void killed() {
        agent.killed().get();

        // shot not fail
        assertTrue(true);
    }

    @Test
    public void get() throws JSONException {
        JSONAssert.assertEquals("{\"id\":\"counter\",\"title\":\"counter\",\"properties\":{\"count\":{\"description\":\"current counter content\",\"type\":\"integer\",\"observable\":true},\"lastChange\":{\"description\":\"last change of counter content\",\"type\":\"string\",\"observable\":true,\"readOnly\":true}},\"actions\":{\"decrement\":{},\"increment\":{\"description\":\"Incrementing counter content with optional step content as uriVariable\",\"uriVariables\":{\"step\":{\"type\":\"integer\",\"minimum\":1,\"maximum\":250}},\"input\":{\"type\":\"object\"},\"output\":{\"type\":\"integer\"}},\"reset\":{}},\"events\":{\"change\":{}}}", agent.get().get(), JSONCompareMode.LENIENT);
    }

    @Test
    public void readProperties() throws ContentCodecException {
        Map values = ContentManager.contentToValue(agent.readProperties().get().fromJadex(), new ObjectSchema());

        assertEquals(2, values.size());
        assertEquals(42, values.get("count"));
    }

    @Test
    public void readProperty() throws ContentCodecException {
        int value = ContentManager.contentToValue(agent.readProperty("count").get().fromJadex(), new IntegerSchema());
        assertEquals(42, value);
    }

    @Test
    public void writeProperty() throws ContentCodecException {
        agent.writeProperty("count",new JadexContent("application/json", "1337".getBytes())).get();

        int value = ContentManager.contentToValue(agent.readProperty("count").get().fromJadex(), new IntegerSchema());
        assertEquals(1337, value);
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

        thing.addAction("decrement", new ThingAction(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int newValue = ((Integer) value) - 1;
                thing.getProperty("count").write(newValue);
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return newValue;
            });
        });

        thing.addAction("reset", new ThingAction(), (input, options) -> {
            return thing.getProperty("count").write(0).thenApply(value -> {
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return 0;
            });
        });

        thing.addEvent("change", new ThingEvent());

        return thing;
    }
}