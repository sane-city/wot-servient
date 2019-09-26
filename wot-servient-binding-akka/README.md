This module uses [Akka](https://akka.io/) to connect different distributed nodes running the WoT implementation.

For the discovery the cluster function of Akka is used.
This extension also enables interaction with Things via HTTP.

## Examples

Examples can be found in the `city.sane.wot.examples` package.

## Other stuff

cd /sane-node-akka
mvn clean install
cd ./wot-akka
mvn -DskipTests clean install
mvn -DskipTests clean package

java -cp ./target/wot-akka-1.0-SNAPSHOT.jar -Dconfig.file=/opt/application.conf city.sane.examples.Klimabotschafter

docker build -t git.informatik.uni-hamburg.de:4567/bornholdt/sane-node-akka:wot-akka .

docker run -ti --rm -P git.informatik.uni-hamburg.de:4567/bornholdt/sane-node-akka:wot-akka city.sane.wot.examples.Klimabotschafter