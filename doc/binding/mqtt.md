# MQTT Binding

This binding uses a Message Queuing Telemetry Transport broker to interact with Things.

The Thing Descriptions of all exposed Things are published as retainable messages on the Broker. The topic corresponds to the Thing Description.

## Configuration

To add MQTT support to the Servient, `"city.sane.wot.binding.mqtt.MqttProtocolServer"` must be added to Config parameter `wot.servient.servers` and
`"city.sane.wot.binding.mqtt.MqttProtocolClientFactory"` must be added to parameter
`wot.servient.client-factories`.

All configurations associated with MQTT Binding are located in the `wot.servient.mqtt` namespace:

| Parameter     | Explanation  | Default Value |
|---------------|---------------|---------------|
| `broker`      | Address of broker to connect to, specified as a URI | `"tcp://iot.eclipse.org"`
| `username`    | The username for the connection | |
| `password`    | The password for the connection | |
| `client-id`   | A client identifier that is unique on the broker being connected to | randomly generated value |

### Example
```hocon
wot {
  servient {
    servers = [
      "city.sane.wot.binding.mqtt.MqttProtocolServer",
    ]

    client-factories = [
      "city.sane.wot.binding.mqtt.MqttProtocolClientFactory",
    ]

    mqtt {
      broker = "tcp://iot.eclipse.org"
      username = ""
      password = ""
    }
  }
}
```