# Websocket Binding

This binding uses the WebSocket protocol to interact with Things.

## Configuration

To add Websocket support to the Servient, `"city.sane.wot.binding.http.WebsocketProtocolServer"` must be added to Config parameter `wot.servient.server` and
`"city.sane.wot.binding.websocket.WebsocketProtocolClientFactory" must be added to parameter
`wot.servient.client-factories`.

All configurations associated with Websocket Binding are located in the `wot.servient.websocket` namespace:

| Parameter     | Explanation  | Default Value |
|---------------|---------------|---------------|
| `bind-host`   | IP address that websocket server should listen on. | `"0.0.0.0"`
| `bind-port`   | Port that websocket server should listen on. | `8081` |
| `addresses`   | List of URLs, which are used in the Thing Description as accessible addresses. If no addresses are specified, the service automatically determines its local addresses. However, it may be necessary to set the address manually, for example when using Docker. | `[]` |


### Example
```hocon
wot {
  servient {
    servers = [
      "city.sane.wot.binding.websocket.WebsocketProtocolServer",
    ]

    client-factories = [
      "city.sane.wot.binding.websocket.WebsocketProtocolClientFactory",
    ]

    websocket {
      bind-host = "0.0.0.0"
      bind-port = 8081
      addresses = ["ws://192.168.178.42:8081"]
    }
  }
}
```

### Reading Properties
Send a ReadProperty Message containing the ThingID and the name of the Thing Property as Strings.
Returns the Value of Property `{name}` of Thing `{id}`.

### Writing Properties
Send a WriteProperty Message containing the ThingID and the name of the Thing Property as Strings, as well as the Content that should be written to the Property.
Writes the Value of Property `{name}` of Thing `{id}`.

### Invoking Actions
Send a InvokeAction Message containing the ThingID and the name of the Thing Action as Strings.
Returns the Value of Invoked Action `{name}` of Thing `{id}`.

### Subscribing Properties
Send a SubscribeProperty Message containing the ThingID and the name of the Thing Property as Strings.
Notified when observed Property has changed for Property `{name}` of Thing `{id}`.

### Subscribing Events
Send a SubscribeEvent Message containing the ThingID and the name of the Thing Event as Strings.
Notified when an event has been emitted for Event `{name}` of Thing `{id}`.

