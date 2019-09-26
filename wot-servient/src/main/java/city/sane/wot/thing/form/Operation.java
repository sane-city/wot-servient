package city.sane.wot.thing.form;

/**
 * Defines the operation (e.g. read or write) on which a Thing Interaction is based.
 */
public enum Operation {
    // properties
    readproperty,
    writeproperty,
    observeproperty,
    unobserveproperty,
    readallproperties,
    readmultipleproperties,

    // events
    subscribeevent,
    unsubscribeevent,

    // actions
    invokeaction,
}
