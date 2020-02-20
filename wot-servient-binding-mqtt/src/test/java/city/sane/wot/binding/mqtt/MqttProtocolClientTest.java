package city.sane.wot.binding.mqtt;

import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subject;
import city.sane.wot.thing.observer.Subscription;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

public class MqttProtocolClientTest {
    private MqttProtocolSettings settings;
    private MqttClient mqttClient;
    private Map topicSubjects;
    private MqttProtocolClient client;
    private Form form;
    private Content content;
    private Observer<Content> observer;
    private Subject subject;
    private Subscription subscription;

    @Before
    public void setUp() {
        settings = mock(MqttProtocolSettings.class);
        mqttClient = mock(MqttClient.class);
        topicSubjects = mock(Map.class);
        form = mock(Form.class);
        content = mock(Content.class);
        observer = mock(Observer.class);
        subject = mock(Subject.class);
        subscription = mock(Subscription.class);
    }

    @Test
    public void invokeResourceShouldPublishNullToBroker() throws MqttException {
        when(form.getHref()).thenReturn("tcp://dummy-broker/counter/actions/increment");

        client = new MqttProtocolClient(settings, mqttClient, topicSubjects);
        client.invokeResource(form);

        verify(mqttClient, times(1)).publish(eq("counter/actions/increment"), argThat(new MqttMessageMatcher(new MqttMessage(new byte[0]))));
    }

    @Test
    public void invokeResourceWithContentShouldGivenContentToBroker() throws MqttException {
        when(form.getHref()).thenReturn("tcp://dummy-broker/counter/actions/increment");
        when(content.getBody()).thenReturn("Hallo Welt".getBytes());

        client = new MqttProtocolClient(settings, mqttClient, topicSubjects);
        client.invokeResource(form, content);

        verify(mqttClient, times(1)).publish(eq("counter/actions/increment"), argThat(new MqttMessageMatcher(new MqttMessage("Hallo Welt".getBytes()))));
    }

    @Test
    public void subscribeResourceShouldSubscribeToBroker() throws MqttException {
        when(form.getHref()).thenReturn("tcp://dummy-broker/counter/events/change");
        when(content.getBody()).thenReturn("Hallo Welt".getBytes());

        client = new MqttProtocolClient(settings, mqttClient, topicSubjects);
        client.subscribeResource(form, observer);

        verify(mqttClient, times(1)).subscribe(eq("counter/events/change"), any());
    }

    @Test
    public void subscribeResourceShouldReuseExistingBrokerSubscriptions() {
        when(form.getHref()).thenReturn("tcp://dummy-broker/counter/events/change");
        when(content.getBody()).thenReturn("Hallo Welt".getBytes());

        client = new MqttProtocolClient(settings, mqttClient, new HashMap(Map.of("counter/events/change", subject)));
        client.subscribeResource(form, observer);

        verify(subject, times(1)).subscribe(observer);
    }

    @Test
    public void subscribeResourceShouldUnsubscribeFromBrokerWhenSubscriptionIsNotLongUsed() throws ExecutionException, InterruptedException, MqttException {
        when(form.getHref()).thenReturn("tcp://dummy-broker/counter/events/change");
        when(content.getBody()).thenReturn("Hallo Welt".getBytes());

        client = new MqttProtocolClient(settings, mqttClient, new HashMap(Map.of("counter/events/change", new Subject())));
        Subscription subscription = client.subscribeResource(form, observer).get();
        subscription.unsubscribe();

        verify(mqttClient, times(1)).unsubscribe(eq("counter/events/change"));
    }

    private class MqttMessageMatcher implements ArgumentMatcher<MqttMessage> {
        private final MqttMessage left;

        public MqttMessageMatcher(MqttMessage left) {
            this.left = left;
        }

        @Override
        public boolean matches(MqttMessage right) {
            if (left == right) {
                return true;
            }
            if (right == null) {
                return false;
            }
            else {
                return Arrays.equals(left.getPayload(), right.getPayload());
            }
        }
    }
}