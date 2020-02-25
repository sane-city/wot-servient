package city.sane.wot.binding.mqtt;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import com.typesafe.config.Config;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Allows consuming Things via MQTT. TODO: Currently a MqttProtocolClient and therefore also a
 * MqttClient is created for each Thing. Even if each thing is reachable via the same broker. It
 * would be better if there was only one MqttClient per broker and that is shared by all
 * MqttProtocolClient instances. TODO: MqttClient.close() is never called!
 */
public class MqttProtocolClient implements ProtocolClient {
    private static final Logger log = LoggerFactory.getLogger(MqttProtocolClient.class);
    private final MqttProtocolSettings settings;
    private final Map<String, Observable<Content>> topicSubjects;
    private MqttClient client;

    public MqttProtocolClient(Config config) throws ProtocolClientException {
        try {
            settings = new MqttProtocolSettings(config);
            settings.validate();
            client = settings.createConnectedMqttClient();
            topicSubjects = new HashMap<>();
        }
        catch (MqttProtocolException e) {
            throw new ProtocolClientException(e);
        }
    }

    MqttProtocolClient(MqttProtocolSettings settings,
                       MqttClient mqttClient,
                       Map<String, Observable<Content>> topicSubjects) {

        this.settings = settings;
        client = mqttClient;
        this.topicSubjects = topicSubjects;
    }

    @Override
    public CompletableFuture<Content> invokeResource(Form form) {
        return invokeResource(form, null);
    }

    @Override
    public CompletableFuture<Content> invokeResource(Form form, Content content) {
        CompletableFuture<Content> future = new CompletableFuture<>();

        try {
            String topic = new URI(form.getHref()).getPath().substring(1);

            publishToTopic(content, future, topic);
        }
        catch (URISyntaxException e) {
            future.completeExceptionally(
                    new ProtocolClientException("Unable to extract topic from href '" + form.getHref() + "'"));
        }

        return future;
    }

    @Override
    public Observable<Content> observeResource(Form form) throws ProtocolClientException {
        try {
            String topic = new URI(form.getHref()).getPath().substring(1);

            return topicSubjects.computeIfAbsent(topic, key -> topicObserver(form, topic));
        }
        catch (URISyntaxException e) {
            throw new ProtocolClientException("Unable to subscribe resource: " + e.getMessage());
        }
    }

    @NonNull
    private Observable<Content> topicObserver(Form form, String topic) {
        return Observable.using(
                () -> client,
                myClient -> Observable.<Content>create(source -> {
                    log.debug("MqttClient connected to broker at '{}' subscribe to topic '{}'", settings.getBroker(), topic);

                    try {
                        myClient.subscribe(topic, (receivedTopic, message) -> {
                            log.debug("MqttClient received message from broker '{}' for topic '{}'", settings.getBroker(), receivedTopic);
                            Content content = new Content(form.getContentType(), message.getPayload());
                            source.onNext(content);
                        });
                    }
                    catch (MqttException e) {
                        log.warn("Exception occured while trying to subscribe to broker '{}' and topic '{}': {}", settings.getBroker(), topic, e.getMessage());
                        source.onError(e);
                    }
                }),
                myClient -> {
                    log.debug("MqttClient subscriptions of broker '{}' and topic '{}' has no more observers. Remove subscription.", settings.getBroker(), topic);

                    try {
                        myClient.unsubscribe(topic);
                    }
                    catch (MqttException e) {
                        log.warn("Exception occured while trying to unsubscribe from broker '{}' and topic '{}': {}", settings.getBroker(), topic, e.getMessage());
                    }
                }
        ).share();
    }

    private void publishToTopic(Content content, CompletableFuture<Content> future, String topic) {
        try {
            log.debug("MqttClient at '{}' publishing to topic '{}'", settings.getBroker(), topic);
            byte[] payload;
            if (content != null) {
                payload = content.getBody();
            }
            else {
                payload = new byte[0];
            }
            client.publish(topic, new MqttMessage(payload));

            // MQTT does not support the request-response pattern. return empty message
            future.complete(Content.EMPTY_CONTENT);
        }
        catch (MqttException e) {
            future.completeExceptionally(new ProtocolClientException(
                    "MqttClient at '" + settings.getBroker() + "' cannot publish data for topic '" + topic + "': " + e.getMessage()
            ));
        }
    }
}