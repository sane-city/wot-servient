package city.sane.wot.binding.mqtt;

import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MqttProtocolClientTest {
    private String broker;
    private MqttProtocolClient client;
    private Config config;

    @Before
    public void setUp() throws ProtocolClientException {
        config = ConfigFactory.load();
        broker = config.getString("wot.servient.mqtt.broker");
        client = new MqttProtocolClient(config);
    }

    @Test
    public void invokeResource() throws ExecutionException, InterruptedException {
        String href = broker + "/counter/actions/increment";
        Form form = new Form.Builder().setHref(href).build();

        assertEquals(new Content("application/json", new byte[0]), client.invokeResource(form).get());
    }

    @Test(timeout = 5 * 1000)
    public void subscribeResource() throws ExecutionException, InterruptedException, MqttException {
        String href = broker + "/counter/events/change";
        Form form = new Form.Builder().setHref(href).build();

        CompletableFuture<Void> nextCalledFuture = new CompletableFuture<>();
        assertThat(client.subscribeResource(form, new Observer<>(next -> {
            nextCalledFuture.complete(null);
        })).get(), instanceOf(Subscription.class));

        MqttClient client = new MqttClient(config.getString("wot.servient.mqtt.broker"), MqttClient.generateClientId());
        client.connect();
        client.publish("counter/events/change", new MqttMessage());

        assertNull(nextCalledFuture.get());
    }
}