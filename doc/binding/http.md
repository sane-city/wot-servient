# HTTP Binding

Uses the Hypertext Transfer Protocol for interaction with Things.

## Configuration

To add HTTP support to the Servient, `"city.sane.wot.binding.http.HttpProtocolServer"` must be added to Config parameter `wot.servient.servers` and
`"city.sane.wot.binding.http.HttpProtocolClientFactory"` and `"city.sane.wot.binding.http.HttpsProtocolClientFactory"` must be added to parameter
`wot.servient.client-factories`.

All configurations associated with HTTP Binding are located in the `wot.servient.http` namespace:

| Parameter         | Explanation  | Default Value |
|-------------------|---------------|---------------|
| `bind-host`       | IP address that HTTP server should listen on. | `"0.0.0.0"`
| `bind-port`       | Port that HTTP server should listen on.<br> A port number of `0` means that the port number is automatically allocated. | `80` |
| `addresses`       | List of URLs, which are used in the Thing Description as accessible addresses. If no addresses are specified, the service automatically determines its local addresses. However, it may be necessary to set the address manually, for example when using Docker. | `[]` |
| `security.scheme` | Defines the security mechanism with which the interaction with Things is secured. | `null` |

### Example
```hocon
wot {
  servient {
    servers = [
      "city.sane.wot.binding.http.HttpProtocolServer",
    ]

    client-factories = [
      "city.sane.wot.binding.http.HttpProtocolClientFactory",
      "city.sane.wot.binding.http.HttpsProtocolClientFactory",
    ]

    http {
      bind-host = "0.0.0.0"
      bind-port = 8080
      addresses = ["http://192.168.178.42:8080"]
      security {
        scheme = null
      }
    }
  }
}
```

## Server Endpoints

Here is a list of the available HTTP endpoints of HTTP Binding:

### `GET /`

Outputs the Thing Descriptions of all Things exposed by the Servient.

### `GET /{id}`

Returns the Thing Description of Thing with ID `{id}`.

### `GET /{id}/properties/{name}`

Returns the value of Property `{name}` of Thing `{id}`.

### `GET /{id}/properties/{name}/observable`

Returns changes to the value of Property `{name}` of Thing `{id}`.

### `PUT /{id}/properties/{name}`

Writes a new value for Property `{name}` of Thing `{id}`.
HTTP Long Polling is used here.

### `POST /{id}/actions/{name}`

Invokes the Action `{name}` of Thing `{id}`.

### `GET /{id}/events/{name}`

Notified when an event has been emitted for Event `{name}` of Thing `{id}`.
HTTP Long Polling is used here.

### `GET /{id}/all/properties`

Returns the values of all Properties of Thing `{id}`.

## Security Schemes

### basic
Set `security.scheme` to `"basic""`.

### bearer
Set `security.scheme` to `"bearer""`.