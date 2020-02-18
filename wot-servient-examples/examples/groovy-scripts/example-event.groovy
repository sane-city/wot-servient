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