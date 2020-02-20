package city.sane.wot.binding.mqtt;

import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class MqttProtocolClientTest {
    private String broker;
    private MqttProtocolClient client;
    @Mock
    private MqttClient mqttClient;

    @Before
    public void setUp() throws MqttException {
        MockitoAnnotations.initMocks(this);

        doNothing().when(mqttClient).publish(anyString(), anyObject());
        doAnswer(invocation -> {
            String topic = invocation.getArgument(0, String.class);
            IMqttMessageListener listener = invocation.getArgument(1, IMqttMessageListener.class);
            listener.messageArrived(topic, new MqttMessage());
            return null;
        }).when(mqttClient).subscribe(anyString(), anyObject());

        broker = "tcp://dummy-broker";
        MqttProtocolSettings settings = new MqttProtocolSettings(broker, "dummy-client", "", "");
        client = new MqttProtocolClient(settings, mqttClient);
    }

    @Test
    public void invokeResource() throws ExecutionException, InterruptedException {
        String href = broker + "/counter/actions/increment";
        Form form = new Form.Builder().setHref(href).build();

        assertEquals(Content.EMPTY_CONTENT, client.invokeResource(form).get());
    }

    @Test(expected = ProtocolClientException.class)
    public void invokeResourceBrokenUri() throws Throwable {
        String href = broker + "/co unter/actions/increment";
        Form form = new Form.Builder().setHref(href).build();

        try {
            client.invokeResource(form).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void subscribeResource() throws ExecutionException, InterruptedException, MqttException {
        String href = broker + "/counter/events/change";
        Form form = new Form.Builder().setHref(href).build();

        CompletableFuture<Void> nextCalledFuture = new CompletableFuture<>();
        assertThat(client.subscribeResource(form, new Observer<>(next -> nextCalledFuture.complete(null))).get(), instanceOf(Subscription.class));

        assertNull(nextCalledFuture.get());
    }

    @Test(expected = ProtocolClientException.class)
    public void subscribeResourceBrokenUri() throws Throwable {
        String href = broker + "/cou nter/events/change";
        Form form = new Form.Builder().setHref(href).build();

        try {
            client.subscribeResource(form, new Observer<>(next -> {
            })).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }
}