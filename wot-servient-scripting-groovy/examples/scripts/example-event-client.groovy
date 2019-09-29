wot.fetch('http://localhost:8080/EventSource').thenAccept { td ->
    println('=== TD ===')
    println(td.toJson(true))
    println('==========')

    def source = wot.consume(td)

    source.events.onchange.subscribe(
            { x -> println('onNext: ' + x); },
            { e -> println('onError: ' + e) },
            { -> println('onCompleted') }
    )
    println('Subscribed')
}.join()

println('Press ENTER to exit the client')
System.in.read()