# Protocol Bindings

Here you will find information about the protocols supported by the WoT Servient.
A ProtocolClient is needed to consume things and a ProtocolServer to expose things in the respective format.

## ProtocolClient

### Available Operations

Any interaction with Things (read/write property, observe event, explore, etc.) can be broken down to the following operations:

**readResource:**
Used to read properties, query a single Thing Description or a whole Thing Directory.

**writeResource:**
Is used for writing new values to properties.

**invokeResource:**
Handles the invocation of actions.

**subscribeResource:**
This is used to subscribe to properties or events.

**discover**:
Performs the discovery of local/remote Thing Descriptions.

### Overview of Supported Operations by each Protocol

Because of the characteristics of the respective protocols not all operations are available for each protocol.
This table shows which operations are supported by which protocol.

|                                   | readResource | writeResource | invokeResource | subscribeResource | discover |
|---------------                    |:-------------:|:-------------:|:--------------:|:-----------------:|:--------:|
| [Akka](akka.md)                   |       ✅      |       ✅       |        ✅      |         ✅        |     ✅   |
| [CoAP](coap.md)                   |       ✅      |       ✅       |        ✅      |         ✅        |          |
| [File](file.md)                   |       ✅      |       ✅       |                |         ✅        |          |
| [HTTP](http.md)                   |       ✅      |       ✅       |        ✅      |         ✅        |          |
| [Jadex](jadex.md)                 |       ✅      |       ✅       |        *¹      |        *¹         |     ✅   |
| [JSONPath+HTTP](jsonpath+http.md) |       ✅      |       ✅       |        ✅      |         ✅        |          |
| [MQTT](mqtt.md)                   |               |                |        ✅      |         ✅        |    ✅    |
| [Value](value.md)                 |       ✅      |                |                |                   |          |
| [Websocket](websocket.md)         |       ✅      |       ✅       |        ✅      |          ✅        |         |

*¹ technically possible, but not implemented yet

### Overview of Supported [Security Schemes](../security/README.md) by each Protocol

|                                   | apikey | basic | bearer | cert | digest | nosec | oauth2 | psk | pop | public |
|---------------                    |:------:|:-----:|:------:|:----:|:------:|:-----:|:------:|:---:|:---:|:------:|
| [Akka](akka.md)                   |        |       |        |      |        |       |        |     |     |        |
| [CoAP](coap.md)                   |        |       |        |      |        |       |        |     |     |        |
| [File](file.md)                   |        |       |        |      |        |       |        |     |     |        |
| [HTTP](http.md)                   |        |  ✅  |  ✅  |      |        |       |        |     |     |        |
| [Jadex](jadex.md)                 |        |       |        |      |        |       |        |     |     |        |
| [JSONPath+HTTP](jsonpath+http.md) |        |       |        |      |        |       |        |     |     |        |
| [MQTT](mqtt.md)                   |        |       |        |      |        |       |        |     |     |        |
| [Value](value.md)                 |        |       |        |      |        |       |        |     |     |        |
| [Websocket](websocket.md)         |        |       |        |      |        |       |        |     |     |        |

C: Supported by the client only<br>
S: Supported by the server only

## Add new [Protocol Binding](https://www.w3.org/TR/wot-architecture/#dfn-wot-protocol-binding)

Implement [ProtocolServer](wot-servient/src/main/java/city/sane/wot/binding/ProtocolServer.java) to create a additional server. This
allows to expose things in additional protocols. To use the newly created implementation, it must be added to
`wot.servient.servers` in the configuration.
Existing implementations can be found in the `wot-servient-binding-*` sub modules.

Implement [ProtocolClient](wot-servient/src/main/java/city/sane/wot/binding/ProtocolClient.java) and
[ProtocolClientFactory](src/main/java/city/sane/wot/binding/ProtocolClientFactor.java) to create a additional client.
This allows to consume things in additional protocols. To use the newly created implementation, it must be added to
`wot.servient.clients` in the configuration.
Existing implementations can be found in the `wot-servient-binding-*` package.