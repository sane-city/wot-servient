package city.sane.wot.binding.mqtt;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.schema.StringSchema;
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
import java.util.concurrent.TimeUnit;

/**
 * Allows consuming Things via MQTT. TODO: Currently the client always connects to the MQTT broker
 * defined in the application.conf, no matter which MQTT broker is defined in the Thing Description
 */
public class MqttProtocolClient implements ProtocolClient {
    private static final Logger log = LoggerFactory.getLogger(MqttProtocolClient.class);
    private final Map<String, Observable<Content>> topicSubjects;
    private final Pair<MqttProtocolSettings, MqttClient> settingsClientPair;

    public MqttProtocolClient(Pair<MqttProtocolSettings, MqttClient> settingsClientPair) {
        this(settingsClientPair, new HashMap<>());
    }

    MqttProtocolClient(Pair<MqttProtocolSettings, MqttClient> settingsClientPair,
                       Map<String, Observable<Content>> topicSubjects) {
        this.settingsClientPair = settingsClientPair;
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

    @Override
    public Observable<Thing> discover(ThingFilter filter) {
        return Observable
                .using(
                        () -> null,
                        ignore -> Observable.create(source -> {
                            log.debug("Subscribe to topic '+' to receive all Thing Descriptions.");
                            settingsClientPair.second().subscribe("+", (topic, message) -> {
                                log.debug("Received Message for Discovery with topic '{}': {}", topic, message);
                                Content content = new Content(message.getPayload());
                                String json = ContentManager.contentToValue(content, new StringSchema());
                                Thing thing = Thing.fromJson(json);
                                source.onNext(thing);
                            });
                        }),
                        ignore -> {
                            log.debug("Discovery is completed. Unsubscribe from topic '+'");
                            settingsClientPair.second().unsubscribe("+");
                        }
                )
                .takeUntil(Observable.timer(5, TimeUnit.SECONDS))
                .map(n -> (Thing) n);
    }

    @NonNull
    private Observable<Content> topicObserver(Form form, String topic) {
        return Observable.using(
                () -> null,
                ignore -> Observable.<Content>create(source -> {
                    log.debug("MqttClient connected to broker at '{}' subscribe to topic '{}'", settingsClientPair.first().getBroker(), topic);

                    try {
                        settingsClientPair.second().subscribe(topic, (receivedTopic, message) -> {
                            log.debug("MqttClient received message from broker '{}' for topic '{}'", settingsClientPair.first().getBroker(), receivedTopic);
                            Content content = new Content(form.getContentType(), message.getPayload());
                            source.onNext(content);
                        });
                    }
                    catch (MqttException e) {
                        log.warn("Exception occured while trying to subscribe to broker '{}' and topic '{}': {}", settingsClientPair.first().getBroker(), topic, e.getMessage());
                        source.onError(e);
                    }
                }),
                ignore -> {
                    log.debug("MqttClient subscriptions of broker '{}' and topic '{}' has no more observers. Remove subscription.", settingsClientPair.first().getBroker(), topic);

                    try {
                        settingsClientPair.second().unsubscribe(topic);
                    }
                    catch (MqttException e) {
                        log.warn("Exception occured while trying to unsubscribe from broker '{}' and topic '{}': {}", settingsClientPair.first().getBroker(), topic, e.getMessage());
                    }
                }
        ).share();
    }

    private void publishToTopic(Content content, CompletableFuture<Content> future, String topic) {
        try {
            log.debug("MqttClient at '{}' publishing to topic '{}'", settingsClientPair.first().getBroker(), topic);
            byte[] payload;
            if (content != null) {
                payload = content.getBody();
            }
            else {
                payload = new byte[0];
            }
            settingsClientPair.second().publish(topic, new MqttMessage(payload));

            // MQTT does not support the request-response pattern. return empty message
            future.complete(Content.EMPTY_CONTENT);
        }
        catch (MqttException e) {
            future.completeExceptionally(new ProtocolClientException(
                    "MqttClient at '" + settingsClientPair.first().getBroker() + "' cannot publish data for topic '" + topic + "': " + e.getMessage()
            ));
        }
    }
}