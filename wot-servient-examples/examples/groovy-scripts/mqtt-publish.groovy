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

thing.expose().whenComplete { r, e ->
    if (e == null) {
        println(thing.title + ' ready')
        new Timer().schedule({
            ++counter
            thing.events.counterEvent.emit(counter)
            println('New count ' + counter)
        }, 0, 1000)
    }
    else {
        println('Error: ' + e)
    }
}