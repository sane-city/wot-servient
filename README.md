# SANE Web of Things Servient

This project contains an implementation of the [W3C Web of Things (WoT)](https://www.w3.org/TR/wot-architecture/)
architecture written in Java.

The WoT was created to enable interoperability across IoT platforms and application domains.

WoT provides mechanisms to formally describe IoT interfaces to allow IoT devices and services to communicate with each
other, independent of their underlying implementation, and across multiple networking protocols. In addition, WoT offers a standardized way to define and program IoT behavior.

See also:
https://www.w3.org/TR/wot-architecture/

As this implementation is primarily developed for the research project
[Smart Networks for Urban Participation (SANE)](https://sane.city/), it only covers the parts of the WoT architecture
necessary for the project. However, we are open to contributions.

This implementation was strongly inspired by [node-wot](https://github.com/eclipse/thingweb.node-wot).

You can either include this implementation in your own software stack or use the [command line interface](wot-servient-cli) with the [WoT Scripting API](https://www.w3.org/TR/wot-scripting-api/).

## Requirements

* Java 11

## Installation

### Maven

Either build and install SANE WoT by yourself...
```bash
mvn install
```

...or pull it from public repo:

Add GitLab Maven Repository to `pom.xml`:
```xml
<repositories>
    <repository>
        <id>gitlab-maven</id>
        <url>https://git.informatik.uni-hamburg.de/api/v4/groups/sane-public/-/packages/maven</url>
    </repository>
</repositories>
```

Add SANE WoT as dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>city.sane.wot-servient</groupId>
    <artifactId>wot-servient-all</artifactId>
    <version>1.2</version>
</dependency>
```

### Official Build

https://git.informatik.uni-hamburg.de/sane/wot-servient/-/jobs/artifacts/master/download?job=build-dist

## Usage

### Examples

No time for explanations? Running examples can be found in the
[`wot-servient-examples`](wot-servient-examples/src/main/java/city/sane/wot/examples) sub module.

### Configuration

You can start using this library without defining any configuration, since sensible default values are provided. Later
on you might need to amend the settings to change the default behavior or adapt for specific runtime environments.

The configuration is done in [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md) format in the resource
`application.conf`. You can you can replace `application.conf` by defining `-Dconfig.resource=whatever`,
`-Dconfig.file=whatever`, or `-Dconfig.url=whatever`.

### Create [Servient](https://www.w3.org/TR/wot-architecture/#dfn-servient)

Just create a new [`DefaultWot`](wot-servient/src/main/java/city/sane/wot/DefaultWot.java) object. After that you can start exposing
or consuming Things.

```java
Wot wot = new DefaultWot();

// do your
// awesome stuff
// here

wot.destroy();
```

### Create Thing

#### From Scratch
````java
Thing thing = new Thing.Builder()
        .setId("counter")
        .setTitle("My Counter")
        .setDescription("This is a simple counter thing")
        .build();
````

#### From JSON-File
```java
Thing thing = Thing.fromJson(new File("path/to/thing.json"));
```

Further examples can be found on https://github.com/thingweb/thingweb-playground/tree/master/WebContent/Examples/Valid

### Expose Thing
```java
ExposedThing exposedThing = wot.produce(thing);
exposedThing.addProperty("count", new ThingProperty.Builder()
                .setType("integer")
                .setDescription("current counter value")
                .setObservable(true)
                .setReadOnly(true)
                .build(),
     42);
exposedThing.expose();
```

### Consume Thing
```java
Thing thing = wot.fetch(new URI("http://localhost:8080/counter")).get();
ConsumedThing consumedThing = wot.consume(thing);
Object value = consumedThing.getProperty("count").read().get();
```

### Documentation

More information can be found in the (still very short) [documentation](doc/README.md).

## License

This project is licensed under the [GNU Lesser General Public License v3.0](LICENSE).
