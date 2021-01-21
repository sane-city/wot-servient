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
// internal state, not exposed as Property
def counter = 0

def thing = wot.produce([id: 'EventSource', title: 'EventSource'])

// manually add Interactions
thing
        .addAction(
                'reset',
                {
                    println('Resetting')
                    counter = 0
                })
        .addEvent(
                'onchange',
                [
                        data: [type: 'integer']
                ])
// make available via bindings
thing.expose().whenComplete { r, e ->
    if (e == null) {
        println(thing.title + ' ready')
        new Timer().schedule({
            ++counter
            thing.events['onchange'].emit(counter)
            println('Emitted change ' + counter)
        }, 0, 5000)
    }
    else {
        println('Error: ' + e)
    }
}