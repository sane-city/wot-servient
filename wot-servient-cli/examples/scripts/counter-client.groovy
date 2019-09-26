wot.fetch('coap://localhost:5683/counter').thenAccept { td ->
    println('=== TD ===')
    println(td.toJson(true))
    println('==========')

    def thing = wot.consume(td)

    // read property #1
    def read1 = thing.properties['count'].read().get()
    println('CounterClient: count value is ' + read1)

    // increment property #1
    thing.actions['increment'].invoke().get()
    def inc1 = thing.properties['count'].read().get()
    println('CounterClient: count value after increment #1 is ' + inc1)

    // increment property #2
    thing.actions['increment'].invoke().get()
    def inc2 = thing.properties['count'].read().get()
    println('CounterClient: count value after increment #2 is ' + inc2)

    // decrement property
    thing.actions['increment'].invoke().get()
    def dec1 = thing.properties['count'].read().get()
    println('CounterClient: count value after decrement is ' + dec1)
}.join()