/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
def td = '''{
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
'''

println('=== TD ===')
println(td)
println('==========')

def source = wot.consume(td)

source.events.temperature.observer().subscribe(
        { x -> println('onNext: ' + x) },
        { e -> println('onError: ' + e) },
        { -> println('onCompleted') }
)
println('Subscribed')

new Timer().schedule({
    source.actions.resetCounter.invoke()
    println('Reset counter!')
}, 20000, 20000)