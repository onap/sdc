FROM onap/integration-java11:10.0.0

USER root
ARG JETTY_FOLDER=/app/jetty

ENV JETTY_HOME=$JETTY_FOLDER
ENV JETTY_BASE=$JETTY_FOLDER
ENV JETTY_USER=onap
ENV JETTY_GROUP=onap

RUN mkdir $JETTY_FOLDER && chown onap:onap $JETTY_FOLDER

USER onap

#Download jetty
RUN wget -q https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/${jetty-distribution.version}/jetty-distribution-${jetty-distribution.version}.tar.gz --tries=3 -O $JETTY_FOLDER/jetty.tar.gz && \
        tar xz -C $JETTY_FOLDER -f $JETTY_FOLDER/jetty.tar.gz --strip 1 && \
        rm -rf $JETTY_FOLDER/jetty.tar.gz

RUN sed -i 's/"jetty"/"onap"/g' $JETTY_FOLDER/etc/jetty-setuid.xml

# Download OpenTelemetry Java Agent
RUN mkdir -p $JETTY_FOLDER/otel && \
    wget -q https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${opentelemetry-javaagent.version}/opentelemetry-javaagent.jar \
    --tries=3 -O $JETTY_FOLDER/otel/opentelemetry-javaagent.jar

COPY --chown=onap:onap onap-sdc-backend/catalog-be.war       $JETTY_FOLDER/webappwar/
COPY --chown=onap:onap context.xml       $JETTY_FOLDER/webapps/
COPY --chown=onap:onap files/rewrite-root-to-swagger-ui.xml $JETTY_FOLDER/etc/rewrite-root-to-swagger-ui.xml

COPY --chown=onap:onap startup.sh $JETTY_FOLDER/
COPY --chown=onap:onap files/create_directories.sh $JETTY_FOLDER/
COPY --chown=onap:onap files/create_jetty_modules.sh $JETTY_FOLDER/
COPY --chown=onap:onap files/logback.xml $JETTY_FOLDER/config/catalog-be/
COPY --chown=onap:onap files/ecomp-error-configuration.yaml $JETTY_FOLDER/config/catalog-be/ecomp-error-configuration.yaml
COPY --chown=onap:onap files/error-configuration.yaml $JETTY_FOLDER/config/catalog-be/error-configuration.yaml
COPY --chown=onap:onap files/BE_3_setup_key_and_trust_store.sh $JETTY_FOLDER/
RUN chmod +x /app/jetty/BE_3_setup_key_and_trust_store.sh

RUN chmod 770 $JETTY_FOLDER/startup.sh
RUN chmod +x $JETTY_FOLDER/create_directories.sh && $JETTY_FOLDER/create_directories.sh
RUN chmod +x $JETTY_FOLDER/create_jetty_modules.sh && $JETTY_FOLDER/create_jetty_modules.sh

ENTRYPOINT [ "sh", "-c", "${JETTY_HOME}/startup.sh"]
