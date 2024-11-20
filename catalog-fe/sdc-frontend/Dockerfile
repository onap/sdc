FROM onap/integration-java11:10.0.0

USER root
ARG JETTY_FOLDER=/app/jetty

ENV JETTY_HOME=$JETTY_FOLDER
ENV JETTY_BASE=$JETTY_FOLDER
ENV JETTY_USER=onap
ENV JETTY_GROUP=onap

# Create the jetty folder and necessary config directories
RUN mkdir -p $JETTY_FOLDER/config/catalog-fe && chown -R onap:onap $JETTY_FOLDER

# Install curl for Alpine
RUN apk update && apk add curl

#USER onap

# Download Jetty
RUN wget -q https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.4.51.v20230217/jetty-distribution-9.4.51.v20230217.tar.gz --tries=3 -O $JETTY_FOLDER/jetty.tar.gz && \
    tar xz -C $JETTY_FOLDER -f $JETTY_FOLDER/jetty.tar.gz --strip 1 && \
    rm -rf $JETTY_FOLDER/jetty.tar.gz
RUN sed -i 's/"jetty"/"onap"/g' $JETTY_FOLDER/etc/jetty-setuid.xml

# Add WAR files
ADD --chown=onap:onap onboarding*.war         $JETTY_FOLDER/webapps/
ADD --chown=onap:onap catalog-fe-*.war        $JETTY_FOLDER/webapps/

# Copy startup script
COPY --chown=onap:onap scripts/cleanup_jettydir.sh $JETTY_FOLDER/
COPY --chown=onap:onap scripts/set-http-module.sh $JETTY_FOLDER/
COPY --chown=onap:onap scripts/setup-keystore-truststore.sh $JETTY_FOLDER/

RUN chown -R onap:onap $JETTY_FOLDER/*
RUN chmod 770 $JETTY_FOLDER/cleanup_jettydir.sh && $JETTY_FOLDER/cleanup_jettydir.sh
RUN chmod 770 $JETTY_FOLDER/set-http-module.sh && $JETTY_FOLDER/set-http-module.sh

COPY --chown=onap:onap files/org.onap.sdc.p12 $JETTY_FOLDER/etc/
COPY --chown=onap:onap files/org.onap.sdc.trust.jks $JETTY_FOLDER/etc/

RUN echo "etc/rewrite-root-to-sdc1.xml" >> "/app/jetty/start.d/rewrite.ini"
RUN echo "jetty.httpConfig.sendServerVersion=false" >> "/app/jetty/start.d/start.ini"

USER onap
WORKDIR $JETTY_FOLDER