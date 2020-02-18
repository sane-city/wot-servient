def thing = wot.produce([title: 'DynamicThing'])

// manually add Interactions
thing
        .addAction('addProperty',
                { input, options ->
                    println('Adding Property')
                    thing.addProperty('dynProperty', [type: 'string'], 'available')
                })
        .addAction(
                "remProperty",
                { input, options ->
                    println('Removing Property')
                    thing.removeProperty('dynProperty')
                })

thing.expose()