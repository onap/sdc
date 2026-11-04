FROM eclipse-temurin:11-jre-alpine

USER root
RUN addgroup -S onap && adduser -S -G onap onap
ARG JETTY_FOLDER=/app/jetty

ENV JETTY_BASE=$JETTY_FOLDER

RUN mkdir -p $JETTY_FOLDER/config/catalog-be $JETTY_FOLDER/etc $JETTY_FOLDER/logs $JETTY_FOLDER/otel \
    $JETTY_FOLDER/plugins $JETTY_FOLDER/resources $JETTY_FOLDER/temp && chown -R onap:onap $JETTY_FOLDER

USER onap

# Download the OpenTelemetry Java Agent. The self-signed keystore serves the HTTPS connector unless a deployment
# configures its own; the passwords are the defaults CatalogBeServer uses, as Jetty's distribution did.
RUN wget -q https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${opentelemetry-javaagent.version}/opentelemetry-javaagent.jar \
    --tries=3 -O $JETTY_FOLDER/otel/opentelemetry-javaagent.jar && \
    keytool -genkeypair -alias jetty -keyalg RSA -keysize 2048 -validity 3650 -dname "CN=sdc-be" \
    -storetype JKS -keystore $JETTY_FOLDER/etc/keystore -storepass storepwd -keypass keypwd

COPY --chown=onap:onap onap-sdc-backend/lib/ $JETTY_FOLDER/lib/
COPY --chown=onap:onap onap-sdc-backend/webapp/ $JETTY_FOLDER/webapp/
COPY --chown=onap:onap onap-sdc-backend/catalog-be.jar $JETTY_FOLDER/

COPY --chown=onap:onap startup.sh $JETTY_FOLDER/
COPY --chown=onap:onap files/logback.xml $JETTY_FOLDER/config/catalog-be/
COPY --chown=onap:onap files/ecomp-error-configuration.yaml $JETTY_FOLDER/config/catalog-be/ecomp-error-configuration.yaml
COPY --chown=onap:onap files/error-configuration.yaml $JETTY_FOLDER/config/catalog-be/error-configuration.yaml
COPY --chown=onap:onap files/BE_3_setup_key_and_trust_store.sh $JETTY_FOLDER/
RUN chmod +x /app/jetty/BE_3_setup_key_and_trust_store.sh

RUN chmod 770 $JETTY_FOLDER/startup.sh

WORKDIR $JETTY_FOLDER

ENTRYPOINT [ "sh", "-c", "${JETTY_BASE}/startup.sh"]
