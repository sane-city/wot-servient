package city.sane.wot.binding.mqtt;

import city.sane.Pair;
import city.sane.RefCountResource;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.property.ExposedThingProperty;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.*;

public class MqttProtocolServerTest {
    private MqttProtocolServer server;
    private RefCountResource refCountResource;
    private MqttProtocolSettings settings;
    private MqttClient mqttClient;
    private Pair<MqttProtocolSettings, MqttClient> settingsClientPair;
    private ExposedThing thing;
    private ExposedThingProperty<Object> property;
    private ExposedThingAction action;
    private ExposedThingEvent event;

    @Before
    public void setUp() {
        settings = mock(MqttProtocolSettings.class);
        mqttClient = mock(MqttClient.class);
        refCountResource = mock(RefCountResource.class);
        settingsClientPair = mock(Pair.class);
        thing = mock(ExposedThing.class);
        property = mock(ExposedThingProperty.class);
        action = mock(ExposedThingAction.class);
        event = mock(ExposedThingEvent.class);
    }

    @Test
    public void exposeShouldPublishThingDescription() throws MqttException {
        when(thing.getId()).thenReturn("counter");
        when(settingsClientPair.first()).thenReturn(settings);
        when(settingsClientPair.second()).thenReturn(mqttClient);
        when(settings.getBroker()).thenReturn("tcp://dummy-broker");
        server = new MqttProtocolServer(refCountResource, settingsClientPair);
        server.expose(thing);

        verify(mqttClient).publish(eq("counter"), any());
    }

    @Test
    public void exposeShouldExposeProperties() {
        when(thing.getId()).thenReturn("counter");
        when(thing.getProperties()).thenReturn(Map.of("count", property));
        when(property.observer()).thenReturn(PublishSubject.create());
        when(settingsClientPair.first()).thenReturn(settings);
        when(settingsClientPair.second()).thenReturn(mqttClient);
        when(settings.getBroker()).thenReturn("tcp://dummy-broker");

        server = new MqttProtocolServer(refCountResource, settingsClientPair);
        server.expose(thing);

        verify(property).addForm(any());
    }

    @Test
    public void exposeShouldExposeActions() {
        when(thing.getId()).thenReturn("counter");
        when(thing.getActions()).thenReturn(Map.of("increment", action));
        when(settingsClientPair.first()).thenReturn(settings);
        when(settingsClientPair.second()).thenReturn(mqttClient);
        when(settings.getBroker()).thenReturn("tcp://dummy-broker");

        server = new MqttProtocolServer(refCountResource, settingsClientPair);
        server.expose(thing);

        verify(action).addForm(any());
    }

    @Test
    public void exposeShouldExposeEvents() {
        when(thing.getId()).thenReturn("counter");
        when(thing.getEvents()).thenReturn(Map.of("changed", event));
        when(event.observer()).thenReturn(PublishSubject.create());
        when(settingsClientPair.first()).thenReturn(settings);
        when(settingsClientPair.second()).thenReturn(mqttClient);
        when(settings.getBroker()).thenReturn("tcp://dummy-broker");

        server = new MqttProtocolServer(refCountResource, settingsClientPair);
        server.expose(thing);

        verify(event).addForm(any());
    }
}
