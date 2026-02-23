FROM eclipse-temurin:11-jre-alpine

USER root
ARG JETTY_FOLDER=/app/jetty

RUN addgroup onap && adduser --no-create-home --disabled-password --ingroup onap onap

# wget for fetching jetty, gettext for envsubst
RUN apk update && apk add --no-cache wget gettext

ENV JETTY_HOME=$JETTY_FOLDER
ENV JETTY_BASE=$JETTY_FOLDER
ENV JETTY_USER=onap
ENV JETTY_GROUP=onap

RUN mkdir -p $JETTY_FOLDER && chown onap:onap $JETTY_FOLDER

USER onap

#Download jetty
RUN wget -q https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/${jetty-distribution.version}/jetty-distribution-${jetty-distribution.version}.tar.gz --tries=3 -O $JETTY_FOLDER/jetty.tar.gz && \
        tar xz -C $JETTY_FOLDER -f $JETTY_FOLDER/jetty.tar.gz --strip 1 && \
        rm -rf $JETTY_FOLDER/jetty.tar.gz
RUN sed -i 's/"jetty"/"onap"/g' $JETTY_FOLDER/etc/jetty-setuid.xml

# Create required directories
COPY --chown=onap:onap scripts/create_directories.sh $JETTY_FOLDER/
RUN chmod +x $JETTY_FOLDER/create_directories.sh && $JETTY_FOLDER/create_directories.sh

# Set up Jetty modules
COPY --chown=onap:onap scripts/create_jetty_modules.sh $JETTY_FOLDER/
RUN chmod +x $JETTY_FOLDER/create_jetty_modules.sh && $JETTY_FOLDER/create_jetty_modules.sh

# Copy configuration files
COPY --chown=onap:onap files/webseal.conf.tpl $JETTY_FOLDER/config/sdc-simulator/webseal.conf.tpl
COPY --chown=onap:onap files/log4j2.properties $JETTY_FOLDER/config/sdc-simulator/log4j2.properties
COPY --chown=onap:onap files/http.ini $JETTY_FOLDER/start.d/http.ini
COPY --chown=onap:onap files/https.ini $JETTY_FOLDER/start.d/https.ini
COPY --chown=onap:onap files/ssl.ini $JETTY_FOLDER/start.d/ssl.ini
COPY --chown=onap:onap files/rewrite-root-to-sdc1.xml $JETTY_FOLDER/etc/rewrite-root-to-sdc1.xml

# Copy keystores
COPY --chown=onap:onap files/org.onap.sdc.trust.jks $JETTY_FOLDER/etc/
COPY --chown=onap:onap files/org.onap.sdc.p12 $JETTY_FOLDER/etc/

ADD --chown=onap:onap WSSimulator*.war $JETTY_FOLDER/webapps/
COPY --chown=onap:onap startup.sh $JETTY_FOLDER/

RUN chmod 770 $JETTY_FOLDER/startup.sh

ENTRYPOINT [ "sh", "-c", "${JETTY_HOME}/startup.sh"]
