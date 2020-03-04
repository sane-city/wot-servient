# Akka Binding

Uses [Akka](https://akka.io/) for the interaction with Things.
Akka is a toolkit for building highly concurrent, distributed, and resilient message-driven applications for Java and Scala.
The protocol client and server share an ActorSystem.

The actor system created by this binding uses [Akka Cluster](https://doc.akka.io/docs/akka/current/typed/cluster.html) by default.

## Configuration

To add Akka support to the Servient, `"city.sane.wot.binding.akka.AkkaProtocolServer"` must be added to Config parameter `wot.servient.server` and
`"city.sane.wot.binding.akka.AkkaProtocolClientFactory"` must be added to parameter
`wot.servient.client-factories`.

All configurations associated with Akka Binding are located in the `wot.servient.akka` namespace:

| Parameter         | Explanation  | Default Value |
|-------------------|---------------|---------------|
| `system-name`       | Name of the ActorSystem. | `"wot"`
| `ask-timeout`       | Time how long Akka should wait for answers before the request fails. | `60s`
| `discover-timeout`       | Time how long Akka should wait for answers from a Thing Discovery before the Discovery is considered finished. | `5s`

All parameters in this namespace are passed directly to Akka and are used to configure the underlying actor system (e.g. the parameter
`wot.servient.akka.actor.provider` is passed as `akka.actor.provider` to the ActorSystem).

This documentation only lists the configuration parameters relevant for the WoT Servient. For the general configuration of Akka, please refer to the Akka
Documentation: https://doc.akka.io/docs/akka/current/general/configuration.html#custom-application-conf