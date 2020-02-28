package city.sane.wot.binding.mqtt;

import city.sane.Pair;
import city.sane.RefCountResource;
import city.sane.RefCountResourceException;
import city.sane.wot.Servient;
import city.sane.wot.ServientDiscoveryIgnore;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.ProtocolServerException;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.property.ExposedThingProperty;
import com.typesafe.config.Config;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.concurrent.CompletableFuture.*;

/**
 * Allows exposing Things via MQTT.
 */
@ServientDiscoveryIgnore
public class MqttProtocolServer implements ProtocolServer {
    private static final Logger log = LoggerFactory.getLogger(MqttProtocolServer.class);
    private final Map<String, ExposedThing> things = new HashMap<>();
    private final RefCountResource<Pair<MqttProtocolSettings, MqttClient>> mqttClientProvider;
    private Pair<MqttProtocolSettings, MqttClient> settingsClientPair;

    public MqttProtocolServer(Config config) {
        mqttClientProvider = SharedMqttClientProvider.singleton(config);
    }

    MqttProtocolServer(RefCountResource<Pair<MqttProtocolSettings, MqttClient>> mqttClientProvider,
                       Pair<MqttProtocolSettings, MqttClient> settingsClientPair) {
        this.mqttClientProvider = mqttClientProvider;
        this.settingsClientPair = settingsClientPair;
    }

    @Override
    public CompletableFuture<Void> start(Servient servient) {
        log.info("Start MqttServer");

        if (settingsClientPair != null) {
            return completedFuture(null);
        }

        return runAsync(() -> {
            try {
                settingsClientPair = mqttClientProvider.retain();
            }
            catch (RefCountResourceException e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> stop() {
        log.info("Stop MqttServer");

        if (settingsClientPair != null) {
            return runAsync(() -> {
                try {
                    settingsClientPair = null;
                    mqttClientProvider.release();
                }
                catch (RefCountResourceException e) {
                    throw new CompletionException(e);
                }
            });
        }
        else {
            return completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        String baseUrl = createUrl();
        log.info("MqttServer exposes '{}' at '{}{}/*'", thing.getId(), baseUrl, thing.getId());

        if (settingsClientPair == null) {
            return failedFuture(new ProtocolServerException("Unable to expose thing before MqttServer has been started"));
        }

        things.put(thing.getId(), thing);

        exposeProperties(thing, baseUrl);
        exposeActions(thing, baseUrl);
        exposeEvents(thing, baseUrl);
        exposeTD(thing);
        listenOnMqttMessages();

        return completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        log.info("MqttServer stop exposing '{}' as unique '/{}/*'", thing.getId(), thing.getId());
        things.remove(thing.getId());

        return completedFuture(null);
    }

    private String createUrl() {
        String base = "mqtt" + settingsClientPair.first().getBroker().substring(settingsClientPair.first().getBroker().indexOf("://"));
        if (!base.endsWith("/")) {
            base = base + "/";
        }
        return base;
    }

    private void exposeProperties(ExposedThing thing, String baseUrl) {
        Map<String, ExposedThingProperty<Object>> properties = thing.getProperties();
        properties.forEach((name, property) -> {
            String topic = thing.getId() + "/properties/" + name;

            property.observer()
                    .map(optional -> ContentManager.valueToContent(optional.orElse(null)))
                    .map(content -> new MqttMessage(content.getBody()))
                    .subscribe(
                            mqttMessage -> settingsClientPair.second().publish(topic, mqttMessage),
                            e -> log.warn("MqttServer cannot publish data for topic '{}': {}", topic, e.getMessage()),
                            () -> {
                            }
                    );

            String href = baseUrl + topic;
            Form form = new Form.Builder()
                    .setHref(href)
                    .setContentType(ContentManager.DEFAULT)
                    .setOp(Operation.OBSERVE_PROPERTY, Operation.UNOBSERVE_PROPERTY)
                    .build();
            property.addForm(form);
            log.debug("Assign '{}' to Property '{}'", href, name);
        });
    }

    private void exposeActions(ExposedThing thing, String baseUrl) {
        Map<String, ExposedThingAction<Object, Object>> actions = thing.getActions();
        for (Map.Entry<String, ExposedThingAction<Object, Object>> entry : actions.entrySet()) {
            String name = entry.getKey();
            ExposedThingAction<Object, Object> action = entry.getValue();

            String topic = thing.getId() + "/actions/" + name;
            try {
                settingsClientPair.second().subscribe(topic);

                String href = baseUrl + topic;
                Form form = new Form.Builder()
                        .setHref(href)
                        .setContentType(ContentManager.DEFAULT)
                        .setOp(Operation.INVOKE_ACTION)
                        .build();
                action.addForm(form);
                log.debug("Assign '{}' to Action '{}'", href, name);
            }
            catch (MqttException e) {
                throw new CompletionException(e);
            }
        }
    }

    private void exposeEvents(ExposedThing thing, String baseUrl) {
        Map<String, ExposedThingEvent<Object>> events = thing.getEvents();
        events.forEach((name, event) -> {
            String topic = thing.getId() + "/events/" + name;

            event.observer()
                    .map(optional -> ContentManager.valueToContent(optional.orElse(null)))
                    .map(content -> new MqttMessage(content.getBody()))
                    .subscribe(
                            mqttMessage -> settingsClientPair.second().publish(topic, mqttMessage),
                            e -> log.warn("MqttServer cannot publish data for topic '{}': {}", topic, e.getMessage()),
                            () -> {
                            }
                    );

            String href = baseUrl + topic;
            Form form = new Form.Builder()
                    .setHref(href)
                    .setContentType(ContentManager.DEFAULT)
                    .setOp(Operation.SUBSCRIBE_EVENT, Operation.UNSUBSCRIBE_EVENT)
                    .setOptional("mqtt:qos", 0)
                    .setOptional("mqtt:retain", false)
                    .build();
            event.addForm(form);
            log.debug("Assign '{}' to Event '{}'", href, name);
        });
    }

    private void exposeTD(ExposedThing thing) {
        String topic = thing.getId();
        log.debug("Publish '{}' Thing Description to topic '{}'", thing.getId(), topic);

        try {
            Content content = ContentManager.valueToContent(thing.toJson(true));
            MqttMessage mqttMessage = new MqttMessage(content.getBody());
            mqttMessage.setRetained(true);
            settingsClientPair.second().publish(topic, mqttMessage);
        }
        catch (ContentCodecException | MqttException e) {
            log.warn("Unable to publish thing description to topic '{}': {}", topic, e.getMessage());
        }
    }

    private void listenOnMqttMessages() {
        // connect incoming messages to Thing
        settingsClientPair.second().setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.info("MqttServer lost connection to broker: {}", cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                log.info("MqttServer received message for '{}'", topic);

                String[] segments = topic.split("/", 3);

                if (segments.length == 3) {
                    String thingId = segments[0];

                    ExposedThing thing = things.get(thingId);
                    if (thing != null) {
                        if (segments[1].equals("actions")) {
                            String actionName = segments[2];
                            ExposedThingAction<Object, Object> action = thing.getAction(actionName);
                            actionMessageArrived(message, action);
                        }
                    }
                    else {
                        // Thing not found
                    }
                }
                else {
                    // topic not found
                    log.info("MqttServer received message for unexpected topic '{}'", topic);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // do nothing
            }
        });
    }

    private void actionMessageArrived(MqttMessage message,
                                      ExposedThingAction<Object, Object> action) {
        if (action != null) {
            Content inputContent = new Content(ContentManager.DEFAULT, message.getPayload());
            try {
                Object input = ContentManager.contentToValue(inputContent, action.getInput());
                action.invoke(input);
            }
            catch (ContentCodecException e) {
                log.warn("Unable to parse input", e);
            }
        }
        else {
            // Action not found
        }
    }
}
