import java.util.concurrent.CompletableFuture

def thing = wot.produce([title: 'DynamicThing'])

// manually add Interactions
thing
        .addAction('addProperty',
                { input, options ->
                    println('Adding Property')
                    thing.addProperty('dynProperty', [type: 'string'], 'available')
                    return CompletableFuture.completedFuture(null)
                })
        .addAction(
                "remProperty",
                { input, options ->
                    println('Removing Property')
                    thing.removeProperty('dynProperty')
                    return CompletableFuture.completedFuture(null)
                })

thing.expose()