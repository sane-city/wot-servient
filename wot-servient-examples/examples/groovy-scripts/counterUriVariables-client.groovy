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
wot.fetch('coap://localhost:5683/counter').thenAccept { td ->
    println('=== TD ===')
    println(td.toJson(true))
    println('==========')

    def thing = wot.consume(td)

    // read property #1
    def read1 = thing.properties.count.read().get()
    println('count value is ' + read1)

    // increment property #1 (without step)
    thing.actions.increment.invoke().get()
    def inc1 = thing.properties.count.read().get()
    println('count value after increment #1 is ' + inc1)

    // increment property #2
    thing.actions.increment.invoke(['step': 3]).get()
    def inc2 = thing.properties.count.read().get()
    println('count value after increment #2 (with step 3) is ' + inc2)

    // decrement property
    thing.actions.decrement.invoke().get()
    def dec1 = thing.properties.count.read().get()
    println('count value after decrement is ' + dec1)
}.join()