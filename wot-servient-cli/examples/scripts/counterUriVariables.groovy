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

thing.expose().thenRun { println(thing.title + ' ready') }
