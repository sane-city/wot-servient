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
