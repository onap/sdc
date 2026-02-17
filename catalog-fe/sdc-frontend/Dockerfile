FROM onap/integration-java11:10.0.0

USER root
ARG JETTY_FOLDER=/app/jetty

# Install curl for Alpine
RUN apk update && apk add curl jq gettext

# Create the Jetty folder and necessary config directories
RUN mkdir -p $JETTY_FOLDER/ && chown onap:onap $JETTY_FOLDER

ENV JETTY_HOME=$JETTY_FOLDER
ENV JETTY_BASE=$JETTY_FOLDER
ENV JETTY_USER=onap
ENV JETTY_GROUP=onap

#Download jetty
RUN wget -q https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/${jetty-distribution.version}/jetty-distribution-${jetty-distribution.version}.tar.gz --tries=3 -O $JETTY_FOLDER/jetty.tar.gz && \
        tar xz -C $JETTY_FOLDER -f $JETTY_FOLDER/jetty.tar.gz --strip 1 && \
        rm -rf $JETTY_FOLDER/jetty.tar.gz
RUN sed -i 's/"jetty"/"onap"/g' $JETTY_FOLDER/etc/jetty-setuid.xml

# Download OpenTelemetry Java Agent
RUN mkdir -p $JETTY_FOLDER/otel && \
    wget -q https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${opentelemetry-javaagent.version}/opentelemetry-javaagent.jar \
    --tries=3 -O $JETTY_FOLDER/otel/opentelemetry-javaagent.jar

# Add WAR files
ADD --chown=onap:onap onboarding*.war $JETTY_FOLDER/webapps/
ADD --chown=onap:onap catalog-fe-*.war $JETTY_FOLDER/webapps/

# Add scripts and configuration files
COPY --chown=onap:onap artifacts/scripts/cleanup_jettydir.sh $JETTY_FOLDER/
COPY --chown=onap:onap artifacts/scripts/set-http-module.sh $JETTY_FOLDER/
COPY --chown=onap:onap startup.sh $JETTY_FOLDER/
COPY --chown=onap:onap artifacts/scripts/setup-keystore-truststore.sh $JETTY_FOLDER/
COPY --chown=onap:onap artifacts/scripts/ready-probe.sh $JETTY_FOLDER/
COPY --chown=onap:onap artifacts/files/catalog-fe/* $JETTY_FOLDER/config/catalog-fe/
COPY --chown=onap:onap artifacts/files/fe_etc/* $JETTY_FOLDER/etc/
COPY --chown=onap:onap artifacts/files/logging /tmp/logback.xml
COPY --chown=onap:onap artifacts/files/onboardingfe/* $JETTY_FOLDER/config/onboarding-fe/
COPY --chown=onap:onap artifacts/files/startdConfig/* $JETTY_FOLDER/start.d/
COPY --chown=onap:onap artifacts/scripts/env_variables.sh $JETTY_FOLDER/

# Add keystore and truststore files
COPY --chown=onap:onap artifacts/files/org.onap.sdc.p12 $JETTY_FOLDER/etc/
COPY --chown=onap:onap artifacts/files/org.onap.sdc.trust.jks $JETTY_FOLDER/etc/

# Set permissions
RUN chown -R onap:onap $JETTY_FOLDER/* && \
    chmod 770 $JETTY_FOLDER/startup.sh && \
    chmod 770 $JETTY_FOLDER/set-http-module.sh && $JETTY_FOLDER/set-http-module.sh && \
    chmod 755 $JETTY_FOLDER/setup-keystore-truststore.sh && \
    chmod 755 $JETTY_FOLDER/ready-probe.sh && \
    chmod 755 $JETTY_FOLDER/config/catalog-fe/* && \
    chmod 644 $JETTY_FOLDER/etc/* && \
    chmod 770 /tmp/logback.xml && \
    chmod 755 $JETTY_FOLDER/config/onboarding-fe/* && \
    chmod 755 $JETTY_FOLDER/start.d/*

# Add Jetty rewrite and configuration settings
RUN echo "etc/rewrite-root-to-sdc1.xml" >> "$JETTY_FOLDER/start.d/rewrite.ini" && \
echo "jetty.httpConfig.sendServerVersion=false" >> "$JETTY_FOLDER/start.d/start.ini"

USER onap
WORKDIR $JETTY_FOLDER

ENTRYPOINT [ "sh", "-c", "${JETTY_HOME}/startup.sh"]
