package city.sane.wot.binding.mqtt;

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

/**
 * Allows exposing Things via MQTT.
 */
@ServientDiscoveryIgnore
public class MqttProtocolServer implements ProtocolServer {
    private static final Logger log = LoggerFactory.getLogger(MqttProtocolServer.class);
    private final MqttProtocolSettings settings;
    private final Map<String, ExposedThing> things = new HashMap<>();
    private MqttClient client;

    public MqttProtocolServer(Config config) throws ProtocolServerException {
        settings = new MqttProtocolSettings(config);
        try {
            settings.validate();
        }
        catch (MqttProtocolException e) {
            throw new ProtocolServerException(e);
        }
    }

    MqttProtocolServer(MqttProtocolSettings settings) {
        this.settings = settings;
    }

    @Override
    public CompletableFuture<Void> start() {
        if (client != null) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                client = settings.createConnectedMqttClient();
            }
            catch (MqttProtocolException e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> stop() {
        if (client != null) {
            return CompletableFuture.runAsync(() -> {
                try {
                    log.info("MqttServer try to disconnect from broker at '{}'", settings.getBroker());
                    client.disconnect();
                    client = null;
                    log.info("MqttServer disconnected from broker at '{}'", settings.getBroker());
                }
                catch (MqttException e) {
                    throw new CompletionException(e);
                }
            });
        }
        else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        log.info("MqttServer at '{}' exposes '{}' as unique '/{}/*'", settings.getBroker(), thing.getId(), thing.getId());

        if (client == null) {
            return CompletableFuture.failedFuture(new ProtocolServerException("Unable to expose thing before MqttServer has been started"));
        }

        things.put(thing.getId(), thing);

        exposeProperties(thing);
        exposeActions(thing);
        exposeEvents(thing);
        listenOnMqttMessages();

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        log.info("MqttServer at '{}' stop exposing '{}' as unique '/{}/*'", settings.getBroker(), thing.getId(), thing.getId());
        things.remove(thing.getId());

        return CompletableFuture.completedFuture(null);
    }

    private void exposeProperties(ExposedThing thing) {
        Map<String, ExposedThingProperty<Object>> properties = thing.getProperties();
        properties.forEach((name, property) -> {
            String topic = thing.getId() + "/properties/" + name;

            property.subscribe(data -> handleSubscriptionData(topic, data));

            String href = createUrl() + topic;
            Form form = new Form.Builder()
                    .setHref(href)
                    .setContentType(ContentManager.DEFAULT)
                    .setOp(Operation.OBSERVE_PROPERTY, Operation.UNOBSERVE_PROPERTY)
                    .build();
            property.addForm(form);
            log.debug("Assign '{}' to Property '{}'", href, name);
        });
    }

    private void exposeActions(ExposedThing thing) {
        Map<String, ExposedThingAction<Object, Object>> actions = thing.getActions();
        for (Map.Entry<String, ExposedThingAction<Object, Object>> entry : actions.entrySet()) {
            String name = entry.getKey();
            ExposedThingAction<Object, Object> action = entry.getValue();

            String topic = thing.getId() + "/actions/" + name;
            try {
                client.subscribe(topic);

                String href = createUrl() + topic;
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

    private void exposeEvents(ExposedThing thing) {
        Map<String, ExposedThingEvent<Object>> events = thing.getEvents();
        events.forEach((name, event) -> {
            String topic = thing.getId() + "/events/" + name;

            event.subscribe(data -> handleSubscriptionData(topic, data));

            String href = createUrl() + topic;
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

    private void listenOnMqttMessages() {
        // connect incoming messages to Thing
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.info("MqttServer at '{}' lost connection to broker: {}", settings.getBroker(), cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                log.info("MqttServer at '{}' received message for '{}'", settings.getBroker(), topic);

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
                    log.info("MqttServer at '{}' received message for invalid topic '{}'", settings.getBroker(), topic);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // do nothing
            }
        });
    }

    private void handleSubscriptionData(String topic, Object data) {
        try {
            Content content = ContentManager.valueToContent(data);
            log.debug("MqttServer at '{}' publishing new data to topic '{}'", settings.getBroker(), topic);
            client.publish(topic, new MqttMessage(content.getBody()));
        }
        catch (ContentCodecException e) {
            log.warn("MqttServer at '{}' cannot process data for topic '{}': {}", settings.getBroker(), topic, e.getMessage());
        }
        catch (MqttException e) {
            log.warn("MqttServer at '{}' cannot publish data for topic '{}': {}", settings.getBroker(), topic, e.getMessage());
        }
    }

    private String createUrl() {
        String base = "mqtt" + settings.getBroker().substring(settings.getBroker().indexOf("://"));
        if (!base.endsWith("/")) {
            base = base + "/";
        }
        return base;
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
