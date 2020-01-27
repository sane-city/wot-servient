FROM openjdk:11-jre-slim

RUN mkdir /usr/local/share/wot-servient && \
    ln -s ../share/wot-servient/bin/wot-servient /usr/local/bin/wot-servient

COPY ./wot-servient-cli/bin/wot-servient /usr/local/share/wot-servient/bin/wot-servient
COPY ./wot-servient-cli/target/lib/ /usr/local/share/wot-servient/lib/
COPY ./wot-servient-cli/target/wot-servient-cli.jar /usr/local/share/wot-servient/wot-servient-cli.jar

# http coap
EXPOSE 8080 5683

ENTRYPOINT ["wot-servient"]