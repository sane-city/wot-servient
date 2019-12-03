package city.sane.wot.binding.mqtt;

import city.sane.wot.binding.ProtocolServer;
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
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Allows exposing Things via MQTT.
 */
public class MqttProtocolServer implements ProtocolServer {
    static final Logger log = LoggerFactory.getLogger(MqttProtocolServer.class);

    private final String broker;
    private final String clientId;
    private final String username;
    private final String password;
    private final Map<String, ExposedThing> things = new HashMap<>();
    private MqttClient client;

    public MqttProtocolServer(Config config) {
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
    }

    @Override
    public CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            if (broker == null || broker.isEmpty()) {
                log.warn("No broker defined for MQTT server binding - skipping");
            }
            else {
                try {
                    MqttClientPersistence persistence = new MemoryPersistence();
                    client = new MqttClient(broker, clientId, persistence);

                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setCleanSession(true);
                    if (username != null) {
                        options.setUserName(username);
                    }
                    if (password != null) {
                        options.setPassword(password.toCharArray());
                    }

                    log.info("MqttServer trying to connect to broker at '{}' with client ID '{}'", broker, clientId);
                    client.connect(options);
                    log.info("MqttServer connected to broker at '{}'", broker);
                }
                catch (MqttException e) {
                    log.error("MqttServer could not connect to broker at '{}': {}", broker, e.getMessage());
                    throw new CompletionException(e);
                }
            }
        });
    }

    @Override
    public CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("MqttServer try to disconnect from broker at '{}'", broker);
                client.disconnect();
                log.info("MqttServer disconnected from broker at '{}'", broker);
            }
            catch (MqttException e) {
                log.error("MqttServer could not disconnect from broker at '{}': {}", broker, e.getMessage());
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        if (client == null) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("MqttServer at '{}' exposes '{}' as unique '/{}/*'", broker, thing.getTitle(), thing.getId());

        things.put(thing.getId(), thing);

        exposeProperties(thing);
        exposeActions(thing);
        exposeEvents(thing);

        // connect incoming messages to Thing
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.info("MqttServer at '{}' lost connection to broker: {}", broker, cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                log.info("MqttServer at '{}' received message for '{}'", broker, topic);

                String[] segments = topic.split("/", 3);

                if (segments.length == 3) {
                    String thingId = segments[0];

                    ExposedThing thing = things.get(thingId);
                    if (thing != null) {
                        if (segments[1].equals("actions")) {
                            String actionName = segments[2];
                            ExposedThingAction action = thing.getAction(actionName);
                            if (action != null) {
                                Content inputContent = new Content(ContentManager.DEFAULT, message.getPayload());
                                try {
                                    Object input = ContentManager.contentToValue(inputContent, action.getInput());

                                    action.invoke(input);
                                }
                                catch (ContentCodecException e) {
                                    log.warn("Unable to parse input: {}", e);
                                }
                            }
                            else {
                                // Action not found
                            }
                        }

                    }
                    else {
                        // Thing not found
                    }
                }
                else {
                    // topic not found
                    log.info("MqttServer at '{}' received message for invalid topic '{}'", broker, topic);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // do nothing
            }
        });

        return CompletableFuture.completedFuture(null);
    }

    private void exposeProperties(ExposedThing thing) {
        Map<String, ExposedThingProperty> properties = thing.getProperties();
        properties.forEach((name, property) -> {
            String topic = thing.getId() + "/properties/" + name;

            property.subscribe(data -> {
                try {
                    Content content = ContentManager.valueToContent(data);
                    log.info("MqttServer at '{}' publishing new data to Property topic '{}'", broker, topic);
                    client.publish(topic, new MqttMessage(content.getBody()));
                }
                catch (ContentCodecException e) {
                    log.warn("MqttServer at '{}' cannot process data for Property '{}': {}", broker, name, e.getMessage());
                }
                catch (MqttException e) {
                    log.warn("MqttServer at '{}' cannot publish data for Property '{}': {}", broker, name, e.getMessage());
                }
            });

            String href = createUrl() + topic;
            Form form = new Form.Builder()
                    .setHref(href)
                    .setContentType(ContentManager.DEFAULT)
                    .setOp(Arrays.asList(Operation.observeproperty, Operation.unobserveproperty))
                    .build();
            property.addForm(form);
            log.info("Assign '{}' to Property '{}'", href, name);
        });
    }

    private void exposeActions(ExposedThing thing) {
        Map<String, ExposedThingAction> actions = thing.getActions();
        for (Map.Entry<String, ExposedThingAction> entry : actions.entrySet()) {
            String name = entry.getKey();
            ExposedThingAction action = entry.getValue();

            String topic = thing.getId() + "/actions/" + name;
            try {
                client.subscribe(topic);

                String href = createUrl() + topic;
                Form form = new Form.Builder()
                        .setHref(href)
                        .setContentType(ContentManager.DEFAULT)
                        .setOp(Operation.invokeaction)
                        .build();
                action.addForm(form);
                log.info("Assign '{}' to Action '{}'", href, name);
            }
            catch (MqttException e) {
                throw new CompletionException(e);
            }
        }
    }

    private void exposeEvents(ExposedThing thing) {
        Map<String, ExposedThingEvent> events = thing.getEvents();
        events.forEach((name, event) -> {
            String topic = thing.getId() + "/events/" + name;

            event.subscribe((data) -> {
                try {
                    Content content = ContentManager.valueToContent(data);
                    log.info("MqttServer at '{}' publishing new data to Event topic '{}'", broker, topic);
                    client.publish(topic, new MqttMessage(content.getBody()));
                }
                catch (ContentCodecException e) {
                    log.warn("MqttServer at '{}' cannot process data for Event '{}': {}", broker, name, e.getMessage());
                }
                catch (MqttException e) {
                    log.warn("MqttServer at '{}' cannot publish data for Event '{}': {}", broker, name, e.getMessage());
                }
            });

            String href = createUrl() + topic;
            Form form = new Form.Builder()
                    .setHref(href)
                    .setContentType(ContentManager.DEFAULT)
                    .setOp(Arrays.asList(Operation.subscribeevent, Operation.unsubscribeevent))
                    .setOptional("mqtt:qos", 0)
                    .setOptional("mqtt:retain", false)
                    .build();
            event.addForm(form);
            log.info("Assign '{}' to Event '{}'", href, name);
        });
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        log.info("MqttServer at '{}' stop exposing '{}' as unique '/{}/*'", broker, thing.getTitle(), thing.getId());
        things.remove(thing.getId());

        return CompletableFuture.completedFuture(null);
    }

    private String createUrl() {
        String base = "mqtt" + broker.substring(broker.indexOf("://"));
        if (!base.endsWith("/")) {
            base = base + "/";
        }
        return base;
    }
}
