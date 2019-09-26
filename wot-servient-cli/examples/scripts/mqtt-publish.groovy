def counter = 0

def thing = wot.produce([
        id         : 'MQTT-Test',
        title      : 'MQTT-Test',
        description: 'Tests a MQTT client that published counter values as an WoT event and subscribes the resetCounter topic as WoT action to reset the own counter.'
])

println('Setup MQTT broker address/port details in wot-servient.conf.json (also see sample in wot-servient.conf.json_mqtt)!')

thing
        .addAction(
                'resetCounter',
                {
                    println('Resetting counter')
                    counter = 0
                })
        .addEvent(
                'counterEvent',
                [
                        type: 'integer'
                ])

thing.expose().thenRun {
    println(thing.title + ' ready')
    new Timer().schedule({
        ++counter
        thing.events.counterEvent.emit(counter)
        println('New count ' + counter)
    }, 0, 1000)
}