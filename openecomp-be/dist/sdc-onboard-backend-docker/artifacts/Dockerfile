FROM eclipse-temurin:11-jre-alpine


USER root
ARG JETTY_FOLDER=/app/jetty

# Install necessary packages and dependencies
RUN set -ex && \
    apk update && \
    apk upgrade --no-cache && \
    apk add --no-cache --virtual .build-deps \
        build-base libffi-dev libxml2-dev libressl-dev wget

ENV JETTY_BASE=$JETTY_FOLDER
ENV JETTY_USER=onap
ENV JETTY_GROUP=onap


RUN addgroup -S $JETTY_GROUP && adduser -S $JETTY_USER -G $JETTY_GROUP && \
    mkdir -p $JETTY_FOLDER/config/onboarding-be $JETTY_FOLDER/etc $JETTY_FOLDER/logs $JETTY_FOLDER/otel \
    $JETTY_FOLDER/resources $JETTY_FOLDER/temp $JETTY_FOLDER/webapps && chown -R $JETTY_USER:$JETTY_GROUP $JETTY_FOLDER

USER onap

# Download OpenTelemetry Java Agent
RUN wget -q https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${opentelemetry-javaagent.version}/opentelemetry-javaagent.jar \
    --tries=3 -O $JETTY_FOLDER/otel/opentelemetry-javaagent.jar

COPY --chown=onap:onap lib/ $JETTY_FOLDER/lib/
COPY --chown=onap:onap onboarding-be-*.jar $JETTY_FOLDER/onboarding-be.jar
COPY --chown=onap:onap api-docs.war $JETTY_FOLDER/webapps/
COPY --chown=onap:onap startup.sh $JETTY_FOLDER/

RUN chmod 770 $JETTY_FOLDER/startup.sh

#Copy configuration files
COPY --chown=onap:onap files/logback.xml $JETTY_FOLDER/config/onboarding-be/
COPY --chown=onap:onap files/org.onap.sdc.p12 $JETTY_FOLDER/etc/
COPY --chown=onap:onap files/org.onap.sdc.trust.jks $JETTY_FOLDER/etc/
COPY --chown=onap:onap files/base_template.yaml $JETTY_BASE/resources/
COPY --chown=onap:onap files/base_template.env $JETTY_BASE/resources/

USER root
RUN apk del .build-deps

USER onap
WORKDIR $JETTY_FOLDER
ENTRYPOINT ["sh", "-c", "${JETTY_BASE}/startup.sh"]
