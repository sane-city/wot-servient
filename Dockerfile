FROM kubeless/unzip AS build

ADD ./wot-servient-*.zip .

RUN unzip -qq ./wot-servient-*.zip && \
    rm ./wot-servient-*.zip

FROM openjdk:11-jre-slim

RUN mkdir /usr/local/share/wot-servient && \
    ln -s ../share/wot-servient/bin/wot-servient /usr/local/bin/wot-servient

COPY --from=build ./wot-servient-* /usr/local/share/wot-servient/

# http coap
EXPOSE 8080 5683

ENTRYPOINT ["wot-servient"]