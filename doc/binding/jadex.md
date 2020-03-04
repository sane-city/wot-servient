# Jadex Binding

Uses [Jadex](https://www.activecomponents.org/) for the interaction with Things.
Jadex enables the development of distributed service-based applications.
The protocol client and server share an Jadex Platform.

## Configuration

To add Websocket support to the Servient, `"city.sane.wot.binding.jadex.JadexProtocolServer"` must be added to Config parameter `wot.servient.servers` and
`"city.sane.wot.binding.jadex.JadexProtocolClientFactory"` must be added to parameter
`wot.servient.client-factories`.