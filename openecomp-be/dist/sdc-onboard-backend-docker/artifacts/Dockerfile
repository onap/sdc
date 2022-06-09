FROM onap/integration-java11:9.0.0

USER root
ARG JETTY_FOLDER=/app/jetty

# Install Chef
RUN set -ex && \
    apk update && \
    apk add --no-cache \
        build-base \
        ruby \
        ruby-dev \
        libffi-dev \
        libxml2-dev \
        libressl-dev && \
    gem install multipart-post -v 2.2.0 && \
    gem install chef:13.8.5 berkshelf:6.3.1 io-console:0.4.6 etc webrick --no-document && \
    gem cleanup && \
    apk update

ENV JETTY_HOME=$JETTY_FOLDER
ENV JETTY_BASE=$JETTY_FOLDER
ENV JETTY_USER=onap
ENV JETTY_GROUP=onap

RUN mkdir $JETTY_FOLDER && chown onap:onap $JETTY_FOLDER

USER onap

#Download jetty
RUN wget https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/${jetty-distribution.version}/jetty-distribution-${jetty-distribution.version}.tar.gz -O $JETTY_FOLDER/jetty.tar.gz && \
        tar xvz -C $JETTY_FOLDER -f $JETTY_FOLDER/jetty.tar.gz --strip 1 && \
        rm -rf $JETTY_FOLDER/jetty.tar.gz
RUN sed -i 's/"jetty"/"onap"/g' $JETTY_FOLDER/etc/jetty-setuid.xml

COPY --chown=onap:onap chef-solo $JETTY_FOLDER/chef-solo/
COPY --chown=onap:onap chef-repo/cookbooks $JETTY_FOLDER/chef-solo/cookbooks/
ADD --chown=onap:onap onboarding-be-*.war    $JETTY_FOLDER/webapps/
ADD --chown=onap:onap api-docs.war           $JETTY_FOLDER/webapps/
COPY --chown=onap:onap startup.sh $JETTY_FOLDER/

RUN chmod 770 $JETTY_FOLDER/startup.sh

ENTRYPOINT [ "sh", "-c", "${JETTY_HOME}/startup.sh"]
