package city.sane.wot.binding.mqtt;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subject;
import city.sane.wot.thing.observer.Subscription;
import com.typesafe.config.Config;
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
 * Allows consuming Things via MQTT.
 * TODO: Currently a MqttProtocolClient and therefore also a MqttClient is created for each Thing. Even if each thing is reachable via the same broker. It would be better if there was only one MqttClient per broker and that is shared by all MqttProtocolClient instances.
 * TODO: MqttClient.close() is never called!
 */
public class MqttProtocolClient implements ProtocolClient {
    private static final Logger log = LoggerFactory.getLogger(MqttProtocolClient.class);

    private final MqttProtocolSettings settings;
    private final Map<String, Subject<Content>> topicSubjects = new HashMap<>();
    private MqttClient client;

    public MqttProtocolClient(Config config) throws ProtocolClientException {
        settings = new MqttProtocolSettings(config);
        try {
            settings.validate();
            client = settings.createConnectedMqttClient();
        }
        catch (MqttProtocolException e) {
            throw new ProtocolClientException(e);
        }
    }

    MqttProtocolClient(MqttProtocolSettings settings, MqttClient client) {
        this.settings = settings;
        this.client = client;
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

    private void publishToTopic(Content content, CompletableFuture<Content> future, String topic) {
        try {
            log.info("MqttClient at '{}' publishing to topic '{}'", settings.getBroker(), topic);
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

    @Override
    public CompletableFuture<Content> invokeResource(Form form) {
        return invokeResource(form, null);
    }

    @Override
    public CompletableFuture<Subscription> subscribeResource(Form form, Observer<Content> observer) {
        String topic;
        try {
            topic = new URI(form.getHref()).getPath().substring(1);
        }
        catch (URISyntaxException e) {
            return CompletableFuture.failedFuture(new ProtocolClientException("Unable to subscribe resource: " + e.getMessage()));
        }

        Subject<Content> newSubject = new Subject<>();
        Subject<Content> existingSubject = topicSubjects.putIfAbsent(topic, newSubject);
        CompletableFuture<Subscription> result = new CompletableFuture<>();
        if (existingSubject == null) {
            // first subscription for this mqtt topic. create new subject and create subscription
            Subscription subscription = newSubject.subscribe(observer);

            CompletableFuture.runAsync(() -> {
                log.info("MqttClient connected to broker at '{}' subscribe to topic '{}'", settings.getBroker(), topic);

                try {
                    client.subscribe(topic, (receivedTopic, message) -> {
                        log.info("MqttClient received message from broker '{}' for topic '{}'", settings.getBroker(), receivedTopic);
                        Content content = new Content(form.getContentType(), message.getPayload());
                        newSubject.next(content);
                    });
                }
                catch (MqttException e) {
                    log.warn("Exception occured while trying to subscribe to broker '{}' and topic '{}': {}", settings.getBroker(), topic, e.getMessage());
                    newSubject.error(e);
                }
            }).thenApply(done -> result.complete(subscription));

            existingSubject = newSubject;
        }
        else {
            log.info("MqttClient connected to broker at '{}' reuse existing subscription to topic '{}'", settings.getBroker(), topic);
            Subscription subscription = existingSubject.subscribe(observer);
            result.complete(subscription);
        }

        // attach to created subscriptions because we want to be notified on unsubscription, because we want to remove the mqtt subscription
        final Subject<Content> subject = existingSubject;
        result.thenApply(subscription -> {
            subscription.add(() -> {
                if (subject.getObservers().isEmpty()) {
                    log.debug("MqttClient subscriptions of broker '{}' and topic '{}' has no more observers. Remove subscription.", settings.getBroker(), topic);
                    topicSubjects.remove(topic);
                    subject.complete();
                    try {
                        client.unsubscribe(topic);
                    }
                    catch (MqttException e) {
                        log.warn("Exception occured while trying to unsubscribe from broker '{}' and topic '{}': {}", settings.getBroker(), topic, e.getMessage());
                    }
                }
            });
            return subscription;
        });

        return result;
    }
}