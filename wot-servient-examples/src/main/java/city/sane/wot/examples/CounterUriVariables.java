package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.property.ThingProperty;

import java.util.Map;

/**
 * Produces and exposes a counter thing with variables for interaction.
 */
@SuppressWarnings({ "java:S106", "java:S1192" })
class CounterUriVariables {
    public static void main(String[] args) throws WotException {
        // create wot
        Wot wot = new DefaultWot();

        // create counter
        Thing thing = new Thing.Builder()
                .setId("counter")
                .setTitle("counter")
                .setDescription("counter example Thing")
                .build();
        ExposedThing exposedThing = wot.produce(thing);

        System.out.println("Produced " + exposedThing.getId());

        // expose counter
        exposedThing.addProperty("count",
                new ThingProperty.Builder()
                        .setType("integer")
                        .setDescription("current counter value")
                        .setObservable(true)
                        .setReadOnly(true)
                        .build(),
                42);

        exposedThing.addAction("increment",
                new ThingAction.Builder()
                        .setDescription("Incrementing counter value with optional step value as uriVariable")
                        .setUriVariables(Map.of(
                                "step", Map.of(
                                        "type", "integer",
                                        "minimum", 1,
                                        "maximum", 250
                                )
                        ))
                        .build(),
                (input, options) -> {
                    System.out.println("CounterUriVariables: Incrementing, input= " + input + ", options= " + options);
                    exposedThing.getProperty("count").read().thenApply(value -> {
                        int step;
                        if (input != null && ((Map) input).containsKey("step")) {
                            step = (Integer) ((Map) input).get("step");
                        }
                        else if (options.containsKey("uriVariables") && options.get("uriVariables").containsKey("step")) {
                            step = (int) options.get("uriVariables").get("step");
                        }
                        else {
                            step = 1;
                        }

                        int newValue = ((Integer) value) + step;
                        exposedThing.getProperty("count").write(newValue);
                        return newValue;
                    });
                }
        );

        exposedThing.addAction("decrement",
                new ThingAction.Builder()
                        .setDescription("Decrementing counter value with optional step value as uriVariable")
                        .setUriVariables(Map.of(
                                "step", Map.of(
                                        "type", "integer",
                                        "minimum", 1,
                                        "maximum", 250
                                )
                        ))
                        .build(),
                (input, options) -> {
                    System.out.println("CounterUriVariables: Decrementing, input= " + input + ", options= " + options);
                    exposedThing.getProperty("count").read().thenApply(value -> {
                        int step = 1;
                        if (options.get("uriVariables") != null && options.get("uriVariables").get("step") != null) {
                            step = (int) options.get("uriVariables").get("step");
                        }
                        int newValue = ((Integer) value) - step;
                        exposedThing.getProperty("count").write(newValue);
                        return newValue;
                    });
                }
        );

        exposedThing.expose();
    }
}
