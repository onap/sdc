FROM onap/integration-java11:10.0.0

# Switch to root user for system installations
USER root
ARG JETTY_FOLDER=/app/jetty

FROM onap/integration-java11:10.0.0

USER root
ARG JETTY_FOLDER=/app/jetty

# Install necessary packages and dependencies
RUN set -ex && \
    apk update && \
    apk add --no-cache --virtual .build-deps \
        build-base libffi-dev libxml2-dev libressl-dev wget

ENV JETTY_HOME=$JETTY_FOLDER
ENV JETTY_BASE=$JETTY_FOLDER
ENV JETTY_USER=onap
ENV JETTY_GROUP=onap

RUN mkdir -p $JETTY_FOLDER && chown onap:onap $JETTY_FOLDER

USER onap

# Download jetty
RUN wget -q https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/${jetty-distribution.version}/jetty-distribution-${jetty-distribution.version}.tar.gz \
    --tries=3 -O $JETTY_FOLDER/jetty.tar.gz && \
    tar xz -C $JETTY_FOLDER -f $JETTY_FOLDER/jetty.tar.gz --strip 1 && \
    rm -rf $JETTY_FOLDER/jetty.tar.gz

RUN sed -i 's/"jetty"/"onap"/g' $JETTY_FOLDER/etc/jetty-setuid.xml

# Copy WARs and scripts 
ADD --chown=onap:onap onboarding-be-*.war $JETTY_FOLDER/webapps/
ADD --chown=onap:onap api-docs.war $JETTY_FOLDER/webapps/
COPY --chown=onap:onap startup.sh $JETTY_FOLDER/
COPY --chown=onap:onap scripts/cleanup_jettydir.sh $JETTY_FOLDER/
COPY --chown=onap:onap scripts/create_jetty_modules.sh $JETTY_FOLDER/

# Set executable permissions on the startup script
RUN chmod 770 $JETTY_FOLDER/startup.sh && \
    chmod 770 $JETTY_FOLDER/cleanup_jettydir.sh && $JETTY_FOLDER/cleanup_jettydir.sh && \
    chmod +x $JETTY_FOLDER/create_jetty_modules.sh && $JETTY_FOLDER/create_jetty_modules.sh

#Copy configuration files
COPY --chown=onap:onap files/logback.xml $JETTY_FOLDER/config/onboarding-be/
COPY --chown=onap:onap files/org.onap.sdc.p12 $JETTY_FOLDER/etc/
COPY --chown=onap:onap files/org.onap.sdc.trust.jks $JETTY_FOLDER/etc/
COPY --chown=onap:onap files/base_template.yaml $JETTY_BASE/resources/
COPY --chown=onap:onap files/base_template.env $JETTY_BASE/resources/


USER root
RUN apk del .build-deps

USER onap
ENTRYPOINT ["sh", "-c", "${JETTY_HOME}/startup.sh"]
