FROM eclipse-temurin:11-jre-alpine

USER root
ARG JETTY_FOLDER=/app/jetty

# Create onap user/group, install dependencies, and create the application folder
RUN addgroup -g 101 -S onap && \
    adduser -u 1000 -S -G onap -h /home/onap onap && \
    apk upgrade --no-cache && \
    apk add --no-cache curl jq && \
    mkdir -p $JETTY_FOLDER/etc $JETTY_FOLDER/logs $JETTY_FOLDER/otel && chown -R onap:onap $JETTY_FOLDER
ENV HOME=/home/onap

ENV JETTY_BASE=$JETTY_FOLDER

# Download the OpenTelemetry Java Agent. The self-signed keystore serves the HTTPS connector unless a deployment
# configures its own; the passwords are the defaults FeServer uses, as Jetty's distribution did.
RUN wget -q https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${opentelemetry-javaagent.version}/opentelemetry-javaagent.jar \
    -O $JETTY_FOLDER/otel/opentelemetry-javaagent.jar && \
    keytool -genkeypair -alias jetty -keyalg RSA -keysize 2048 -validity 3650 -dname "CN=sdc-fe" \
    -storetype JKS -keystore $JETTY_FOLDER/etc/keystore -storepass storepwd -keypass keypwd

# Add the application jar, its libraries and the static files of the two UIs
COPY --chown=onap:onap lib/ $JETTY_FOLDER/lib/
COPY --chown=onap:onap webapp/ $JETTY_FOLDER/webapp/
COPY --chown=onap:onap catalog-fe-*.jar $JETTY_FOLDER/catalog-fe.jar

# Add scripts and configuration files
COPY --chown=onap:onap startup.sh $JETTY_FOLDER/
COPY --chown=onap:onap artifacts/scripts/setup-keystore-truststore.sh $JETTY_FOLDER/
COPY --chown=onap:onap artifacts/scripts/ready-probe.sh $JETTY_FOLDER/
COPY --chown=onap:onap artifacts/files/catalog-fe/* $JETTY_FOLDER/config/catalog-fe/
COPY --chown=onap:onap artifacts/files/logging /tmp/logback.xml
COPY --chown=onap:onap artifacts/files/onboardingfe/* $JETTY_FOLDER/config/onboarding-fe/
COPY --chown=onap:onap artifacts/scripts/env_variables.sh $JETTY_FOLDER/

# Add keystore and truststore files
COPY --chown=onap:onap artifacts/files/org.onap.sdc.p12 $JETTY_FOLDER/etc/
COPY --chown=onap:onap artifacts/files/org.onap.sdc.trust.jks $JETTY_FOLDER/etc/

# Set permissions
RUN chown -R onap:onap $JETTY_FOLDER && \
    chmod 770 $JETTY_FOLDER/startup.sh && \
    chmod 755 $JETTY_FOLDER/setup-keystore-truststore.sh $JETTY_FOLDER/ready-probe.sh && \
    chmod 755 $JETTY_FOLDER/config/catalog-fe/* $JETTY_FOLDER/config/onboarding-fe/* && \
    chmod 644 $JETTY_FOLDER/etc/* && chmod 770 /tmp/logback.xml

USER onap
WORKDIR $JETTY_FOLDER

ENTRYPOINT [ "sh", "-c", "${JETTY_BASE}/startup.sh" ]
