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
def thing = wot.produce([
        id         : 'counter',
        title      : 'counter',
        description: 'counter example Thing',
        '@context' : ['https://www.w3.org/2019/wot/td/v1', [iot: 'http://example.org/iot']]
])

print('Produced ' + thing.title)

thing.addProperty(
        'count',
        [
                type        : 'integer',
                description : 'current counter value',
                'iot:Custom': 'example annotation',
                observable  : true,
                readOnly    : false
        ],
        0)

thing.addAction(
        'increment',
        [
                description : 'Incrementing counter value with optional step value as uriVariable',
                input       : ['type': 'object'],
                uriVariables: [
                        step: ['type': 'integer', 'minimum': 1, 'maximum': 250]
                ]
        ],
        { data, options ->
            println('Incrementing, data= ' + data + ', options= ' + options)
            thing.properties['count'].read().thenApply { count ->
                def step = 1
                if (data && 'step' in data) {
                    step = data['step']
                }
                def value = count + step
                thing.properties['count'].write(value)
            }
        })

thing.addAction(
        'decrement',
        [
                description : 'Decrementing counter value with optional step value as uriVariable',
                uriVariables: [
                        step: ['type': 'integer', 'minimum': 1, 'maximum': 250]
                ]
        ],
        { data, options ->
            println('Decrementing ' + options)
            thing.properties['count'].read().thenApply { count ->
                def step = 1
                if (data && 'step' in data) {
                    step = data['step']
                }
                def value = count - step
                thing.properties['count'].write(value)
            }
        })

thing.addAction(
        'reset',
        {
            println('Resetting')
            thing.properties['count'].write(0)
        })

thing.expose().whenComplete { r, e ->
    if (e == null) {
        println(thing.title + ' ready')
    }
    else {
        println('Error: ' + e)
    }
}
