# our base build image
FROM maven:3.6.1-jdk-11

ADD ./ ./

RUN mvn -DskipTests --batch-mode -pl wot-servient-cli -am package

# our final base image
FROM openjdk:11-jre-slim

RUN mkdir /usr/local/share/wot-servient && \
    ln -s ../share/wot-servient/bin/wot-servient /usr/local/bin/wot-servient

COPY --from=0 wot-servient-cli/bin/ /usr/local/share/wot-servient/bin/
COPY --from=0 wot-servient-cli/target/lib/ /usr/local/share/wot-servient/lib/
COPY --from=0 wot-servient-cli/target/wot-servient-cli-1.1-SNAPSHOT.jar /usr/local/share/wot-servient/wot-servient.jar

EXPOSE 8080 5683

ENTRYPOINT ["wot-servient"]