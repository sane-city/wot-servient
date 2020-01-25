# our base build image
FROM maven:3.6.1-jdk-11 AS build
WORKDIR /wot-servient/
COPY ./ ./

RUN mvn -DskipTests --batch-mode -pl wot-servient-cli -am package

# our final base image
FROM openjdk:11-jre-slim

RUN mkdir /usr/local/share/wot-servient && \
    ln -s ../share/wot-servient/bin/wot-servient /usr/local/bin/wot-servient

COPY --from=build /wot-servient/wot-servient-cli/bin/wot-servient /usr/local/share/wot-servient/bin/wot-servient
COPY --from=build /wot-servient/wot-servient-cli/target/lib/ /usr/local/share/wot-servient/lib/
COPY --from=build /wot-servient/wot-servient-cli/target/wot-servient-cli.jar /usr/local/share/wot-servient/wot-servient-cli.jar

EXPOSE 8080 5683

ENTRYPOINT ["wot-servient"]