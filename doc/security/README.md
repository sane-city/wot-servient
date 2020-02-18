# Security Schemes Bindings

Here you will find information about the security schemes supported by the WoT Servient.
Security schemes are intended to restrict interaction with Things to authorized clients.
Various mechanisms are available for authorization.

Further information can be found in [W3C Web of Things Security Ontology](https://www.w3.org/2019/wot/security).

The [Protocol Bindings](../binding/README.md) section describes which protocol supports which authentication mechanism.
In addition, the page for each protocol describes how the server can be configured for the desired authentication mechanism. 

This page describes which credentials have to be defined for the different mechanisms in the service.

## Define Credentials

All credentials are located in the parameter `wot.credentials`. This parameter contains a map with the Thing IDs as keys and
the respective credentials for each Thing.

### Example
```hocon
wot {
  servient {
    ...
    credentials {
      counter {
        username = "foo"
        password = "bar"
      }
      temperatur {
        token = "pu7eevaeH4Ie"
      }
    }
  }
}
```

## basic

| Parameter | Explanation |
|-----------|-------------|
| `username` | The username used for basic authentification.
| `password` | The password used for basic authentification.


## bearer

| Parameter | Explanation |
|-----------|-------------|
| `token` | The token used for bearer authentification.