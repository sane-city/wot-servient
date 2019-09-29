import city.sane.wot.thing.Thing

def td = Thing.fromJson('''{
    "@context": "https://www.w3.org/2019/td/v1",
    "title": "MQTT Counter",
    "id": "urn:dev:wot:mqtt:counter",
    "actions" : {
        "resetCounter": {
            "forms": [
                    {"href": "mqtt://test.mosquitto.org:1883/MQTT-Test/actions/resetCounter",  "mqtt:qos":  0, "mqtt:retain" : false}
            ]
        }
    },
    "events": {
        "temperature": {
            "data": {
                "type": "integer"
            },
            "forms": [
                    {"href": "mqtt://test.mosquitto.org:1883/MQTT-Test/events/counterEvent",  "mqtt:qos":  0, "mqtt:retain" : false}
            ]
        }
    }
}
''')

println('=== TD ===')
println(td.toJson(true))
println('==========')

def source = wot.consume(td)

source.events.temperature.subscribe(
        { x -> println('onNext: ' + x); },
        { e -> println('onError: ' + e) },
        { -> println('onCompleted') }
)
println('Subscribed')

new Timer().schedule({
    source.actions.resetCounter.invoke()
    println('Reset counter!')
}, 20000, 20000)