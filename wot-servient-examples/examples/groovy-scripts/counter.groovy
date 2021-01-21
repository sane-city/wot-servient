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

println('Produced ' + thing.title)

thing.addProperty(
        'count',
        [
                type       : 'integer',
                description: 'current counter vaue',
                observable : true,
                readOnly   : true
        ],
        42
)

thing.addProperty(
        'lastChange',
        [
                type       : 'string',
                description: 'last change of counter value',
                observable : true,
                readOnly   : true
        ],
        new Date().toString()
)

thing.addAction(
        'increment',
        {
            println('Incrementing')
            thing.properties['count'].read().thenApply { count ->
                def value = count + 1
                thing.properties['count'].write(value)
                thing.properties['lastChange'].write(new Date().toString())
                thing.events['change'].emit()
            }
        }
)

thing.addAction(
        'decrement',
        {
            println('Decrementing')
            thing.properties['count'].read().thenApply { count ->
                def value = count - 1
                thing.properties['count'].write(value)
                thing.properties['lastChange'].write(new Date().toString())
                thing.events['change'].emit()
            }
        }
)

thing.addAction(
        'reset',
        {
            println('Resetting')
            thing.properties['count'].write(0)
            thing.properties['lastChange'].write(new Date().toString())
            thing.events['change'].emit()
        }
)

thing.addEvent('change', [:])

thing.expose().whenComplete { r, e ->
    if (e == null) {
        println(thing.title + ' ready')
    }
    else {
        println('Error: ' + e)
    }
}
