# our base build image
FROM maven:3.6.1-jdk-11

ADD ./pom.xml ./pom.xml
ADD ./wot-servient/pom.xml ./wot-servient/pom.xml
ADD ./wot-servient-all/pom.xml ./wot-servient-all/pom.xml
ADD ./wot-servient-binding-akka/pom.xml ./wot-servient-binding-akka/pom.xml
ADD ./wot-servient-binding-coap/pom.xml ./wot-servient-binding-coap/pom.xml
ADD ./wot-servient-binding-file/pom.xml ./wot-servient-binding-file/pom.xml
ADD ./wot-servient-binding-http/pom.xml ./wot-servient-binding-http/pom.xml
ADD ./wot-servient-binding-jadex/pom.xml ./wot-servient-binding-jadex/pom.xml
ADD ./wot-servient-binding-jsonpathhttp/pom.xml ./wot-servient-binding-jsonpathhttp/pom.xml
ADD ./wot-servient-binding-mqtt/pom.xml ./wot-servient-binding-mqtt/pom.xml
ADD ./wot-servient-binding-value/pom.xml ./wot-servient-binding-value/pom.xml
ADD ./wot-servient-cli/pom.xml ./wot-servient-cli/pom.xml
ADD ./wot-servient-examples/pom.xml ./wot-servient-examples/pom.xml

RUN mvn -DskipTests --batch-mode -pl wot-servient-cli -am install dependency:copy-dependencies

ADD ./ ./

RUN mvn -DskipTests --batch-mode -pl wot-servient-cli -am install

# our final base image
FROM openjdk:11-jre-slim

RUN mkdir /usr/local/share/wot-servient && \
    mkdir /usr/local/share/wot-servient/bin && \
    mkdir /usr/local/share/wot-servient/lib && \
    ln -s ../share/wot-servient/bin/wot-servient /usr/local/bin/wot-servient

COPY --from=0 wot-servient-cli/bin/wot-servient /usr/local/share/wot-servient/bin/
COPY --from=0 wot-servient-cli/target/dependency/*.jar /usr/local/share/wot-servient/lib/
COPY --from=0 wot-servient-cli/target/wot-servient-cli-*.jar /usr/local/share/wot-servient/lib/

WORKDIR /wot-servient

EXPOSE 8080 5683

ENTRYPOINT ["wot-servient"]