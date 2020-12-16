package city.sane.wot.binding.mqtt;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.form.Form;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.observers.LambdaObserver;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.subjects.Subject;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MqttProtocolClientTest {
    private MqttProtocolSettings settings;
    private MqttClient mqttClient;
    private Map topicSubjects;
    private MqttProtocolClient client;
    private Form form;
    private Content content;
    private DisposableObserver<Content> observer;
    private Subject subject;
    private Disposable disposable;

    @BeforeEach
    public void setUp() {
        settings = mock(MqttProtocolSettings.class);
        mqttClient = mock(MqttClient.class);
        topicSubjects = mock(Map.class);
        form = mock(Form.class);
        content = mock(Content.class);
        observer = mock(DisposableObserver.class);
        subject = mock(Subject.class);
        disposable = mock(Disposable.class);
    }

    @Test
    public void invokeResourceShouldPublishNullToBroker() throws MqttException {
        when(form.getHref()).thenReturn("tcp://dummy-broker/counter/actions/increment");

        client = new MqttProtocolClient(new Pair(settings, mqttClient), topicSubjects);
        client.invokeResource(form);

        verify(mqttClient).publish(eq("counter/actions/increment"), argThat(new MqttMessageMatcher(new MqttMessage(new byte[0]))));
    }

    @Test
    public void invokeResourceWithContentShouldGivenContentToBroker() throws MqttException {
        when(form.getHref()).thenReturn("tcp://dummy-broker/counter/actions/increment");
        when(content.getBody()).thenReturn("Hallo Welt".getBytes());

        client = new MqttProtocolClient(new Pair(settings, mqttClient), topicSubjects);
        client.invokeResource(form, content);

        verify(mqttClient).publish(eq("counter/actions/increment"), argThat(new MqttMessageMatcher(new MqttMessage("Hallo Welt".getBytes()))));
    }

    @Test
    public void subscribeResourceShouldSubscribeToBroker() throws MqttException, ProtocolClientException {
        when(form.getHref()).thenReturn("tcp://dummy-broker/counter/events/change");

        client = new MqttProtocolClient(new Pair(settings, mqttClient), new HashMap<>());
        client.observeResource(form).subscribe();

        verify(mqttClient).subscribe(eq("counter/events/change"), any());
    }

    @Test
    public void subscribeResourceShouldInformObserverAboutNextValue() throws ProtocolClientException, MqttException, ContentCodecException, ExecutionException, InterruptedException, TimeoutException {
        when(form.getHref()).thenReturn("tcp://dummy-broker/counter/events/change");
        when(form.getContentType()).thenReturn("application/json");
        doAnswer(new AnswersWithDelay(1 * 1000L, invocation -> {
            String topic = invocation.getArgument(0, String.class);
            IMqttMessageListener listener = invocation.getArgument(1, IMqttMessageListener.class);

            listener.messageArrived(topic, new MqttMessage("\"Hallo Welt\"".getBytes()));

            return null;
        })).when(mqttClient).subscribe(any(String.class), any(IMqttMessageListener.class));

        client = new MqttProtocolClient(new Pair(settings, mqttClient), new HashMap<>());

        await().untilAsserted(() -> assertEquals(
                ContentManager.valueToContent("Hallo Welt"),
                client.observeResource(form).firstElement().blockingGet()
        ));
    }

    @Test
    public void subscribeResourceShouldReuseExistingBrokerSubscriptions() throws ProtocolClientException {
        when(form.getHref()).thenReturn("tcp://dummy-broker/counter/events/change");

        LambdaObserver<Content> observer = new LambdaObserver<>(n -> {
        }, e -> {
        }, () -> {
        }, d -> {
        });

        client = new MqttProtocolClient(new Pair(settings, mqttClient), new HashMap(Map.of("counter/events/change", subject)));
        client.observeResource(form).subscribe(observer);

        verify(subject).subscribe(observer);
    }

    @Test
    public void subscribeResourceShouldUnsubscribeFromBrokerWhenSubscriptionIsNotLongUsed() throws MqttException, ProtocolClientException {
        when(form.getHref()).thenReturn("tcp://dummy-broker/counter/events/change");

        LambdaObserver<Content> observer = new LambdaObserver<>(n -> {
        }, e -> {
        }, () -> {
        }, d -> {
        });

        client = new MqttProtocolClient(new Pair(settings, mqttClient), new HashMap());
        client.observeResource(form).subscribe(observer);
        observer.dispose();

        verify(mqttClient).unsubscribe(eq("counter/events/change"));
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