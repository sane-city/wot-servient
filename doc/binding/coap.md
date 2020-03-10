# CoAP Binding

Uses the Constrained Application Protocol for interaction with Things.

## Configuration

To add CoAP support to the Servient, `"city.sane.wot.binding.coap.CoapProtocolServer"` must be added to Config parameter `wot.servient.servers` and
`"city.sane.wot.binding.coap.CoapProtocolClientFactory"` must be added to parameter
`wot.servient.client-factories`.

All configurations associated with COAP Binding are located in the `wot.servient.coap` namespace:

| Parameter         | Explanation  | Default Value |
|-------------------|---------------|---------------|
| `bind-port`       | Port that CoAP server should listen on.<br>A port number of `0` means that the port number is automatically allocated. | `5683` |
| `addresses`       | List of URLs, which are used in the Thing Description as accessible addresses. If no addresses are specified, the service automatically determines its local addresses. However, it may be necessary to set the address manually, for example when using Docker. | `[]` |
| `timeout`        | Sets the maximum amount of time to wait for response. | `60s` |