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
Returns the Value of Property `count` of Thing `counter`.

```json
{"type":"ReadProperty","id":"fc9ca8","thingId":"counter","name":"count"}
```

### Writing Properties
Send a WriteProperty Message containing the ThingID and the name of the Thing Property as Strings, as well as the Content that should be written to the Property. The value is sent via message body.
Writes the Value of Property `count` of Thing `counter`. `body` contains a Base64 encoded byte array containing the Property value.

```json
{"type":"WriteProperty","id":"2f24bd","thingId":"counter","name":"count","value":{"type":"application/json","body":"MTMzNw=="}}
```

### Invoking Actions
Send a InvokeAction Message containing the ThingID and the name of the Thing Action as Strings. Optional values can be send via message body.
Returns the Value of Invoked Action `increment` of Thing `counter`. `body` contains a Base64 encoded byte array containing the Action value.

```json
{"type":"InvokeAction","id":"a04fa6","thingId":"counter","name":"increment","value":{"type":"application/json","body":"eyJzdGVwIjozfQ=="}}
```

### Subscribing Properties
Send a SubscribeProperty Message containing the ThingID and the name of the Thing Property as Strings.
Notified when observed Property has changed for Property `humidty` of Thing `a7910xO18D3k`.

```json
{"type":"SubscribeProperty","id":"829r19","thingId":"a7910xO18D3k","name":"humidity"}
```
### Subscribing Events
Send a SubscribeEvent Message containing the ThingID and the name of the Thing Event as Strings.
Notified when an event has been emitted for Event `change` of Thing `counter`.

```json
{"type":"SubscribeEvent","id":"2e7159","thingId":"counter","name":"change"}
```
