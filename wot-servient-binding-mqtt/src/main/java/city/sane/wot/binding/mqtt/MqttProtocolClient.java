package city.sane.wot.binding.mqtt;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subject;
import city.sane.wot.thing.observer.Subscription;
import com.typesafe.config.Config;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Allows consuming Things via MQTT.
 */
public class MqttProtocolClient implements ProtocolClient {
    static final Logger log = LoggerFactory.getLogger(MqttProtocolClient.class);

    private final String broker;
    private final String clientId;
    private final String username;
    private final String password;
    private final Map<String, Subject<Content>> topicSubjects = new HashMap<>();
    private MqttClient client;

    public MqttProtocolClient(Config config) throws ProtocolClientException {
        if (config.hasPath("wot.servient.mqtt.broker")) {
            broker = config.getString("wot.servient.mqtt.broker");
        }
        else {
            broker = null;
        }

        if (config.hasPath("wot.servient.mqtt.client-id")) {
            clientId = config.getString("wot.servient.mqtt.client-id");
        }
        else {
            clientId = MqttClient.generateClientId();
        }

        if (config.hasPath("wot.servient.mqtt.username")) {
            username = config.getString("wot.servient.mqtt.username");
        }
        else {
            username = null;
        }

        if (config.hasPath("wot.servient.mqtt.password")) {
            password = config.getString("wot.servient.mqtt.password");
        }
        else {
            password = null;
        }

        if (broker == null || broker.isEmpty()) {
            throw new ProtocolClientException("No broker defined for MQTT server binding - skipping");
        }

        try (MqttClientPersistence persistence = new MemoryPersistence()) {
            client = new MqttClient(broker, clientId, persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            if (username != null) {
                options.setUserName(username);
            }
            if (password != null) {
                options.setPassword(password.toCharArray());
            }

            log.info("MqttClient trying to connect to broker at '{}' with client ID '{}'", broker, clientId);
            client.connect(options);
            log.info("MqttClient connected to broker at '{}'", broker);
        }
        catch (MqttException e) {
            log.error("MqttClient could not connect to broker at '{}': {}", broker, e.getMessage());
        }
        // ignore
    }

    @Override
    public CompletableFuture<Content> invokeResource(Form form, Content content) {
        CompletableFuture<Content> future = new CompletableFuture<>();

        try {
            String topic = new URI(form.getHref()).getPath().substring(1);

            try {
                log.info("MqttClient at '{}' publishing to topic '{}'", broker, topic);
                byte[] payload;
                if (content != null) {
                    payload = content.getBody();
                }
                else {
                    payload = new byte[0];
                }
                client.publish(topic, new MqttMessage(payload));

                // MQTT does not support the request-response pattern. return empty message
                future.complete(new Content(ContentManager.DEFAULT, new byte[0]));
            }
            catch (MqttException e) {
                future.completeExceptionally(new ProtocolClientException(
                        "MqttClient at '" + broker + "' cannot publish data for topic '" + topic + "': " + e.getMessage()
                ));
            }
        }
        catch (URISyntaxException e) {
            future.completeExceptionally(
                    new ProtocolClientException("Unable to extract topic from href '" + form.getHref() + "'"));
        }

        return future;
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
            log.warn("Unable to subscribe resource: {}", e);
            return null;
        }

        Subject<Content> newSubject = new Subject<>();
        Subject<Content> existingSubject = topicSubjects.putIfAbsent(topic, newSubject);
        CompletableFuture<Subscription> result = new CompletableFuture<>();
        if (existingSubject == null) {
            // first subscription for this mqtt topic. create new subject and create subscription
            Subscription subscription = newSubject.subscribe(observer);

            CompletableFuture.runAsync(() -> {
                log.info("MqttClient connected to broker at '{}' subscribe to topic '{}'", broker, topic);

                try {
                    client.subscribe(topic, (receivedTopic, message) -> {
                        log.info("MqttClient received message from broker '{}' for topic '{}'", broker, receivedTopic);
                        Content content = new Content(form.getContentType(), message.getPayload());
                        newSubject.next(content);
                    });
                }
                catch (MqttException e) {
                    log.warn("Exception occured while trying to subscribe to broker '{}' and topic '{}': {}", broker, topic, e.getMessage());
                    newSubject.error(e);
                }
            }).thenApply(done -> result.complete(subscription));

            existingSubject = newSubject;
        }
        else {
            log.info("MqttClient connected to broker at '{}' reuse existing subscription to topic '{}'", broker, topic);
            Subscription subscription = existingSubject.subscribe(observer);
            result.complete(subscription);
        }

        // attach to created subscriptions because we want to be notified on unsubscription, because we want to remove the mqtt subscription
        final Subject<Content> subject = existingSubject;
        result.thenApply(subscription -> {
            subscription.add(() -> {
                if (subject.getObservers().isEmpty()) {
                    log.debug("MqttClient subscriptions of broker '{}' and topic '{}' has no more observers. Remove subscription.", broker, topic);
                    topicSubjects.remove(topic);
                    subject.complete();
                    try {
                        client.unsubscribe(topic);
                    }
                    catch (MqttException e) {
                        log.warn("Exception occured while trying to unsubscribe from broker '{}' and topic '{}': {}", broker, topic, e.getMessage());
                    }
                }
            });
            return subscription;
        });

        return result;
    }
}