FROM onap/integration-java11:10.0.0

USER root
ARG JETTY_FOLDER=/app/jetty

# Install Chef
RUN set -ex && \
    apk update && \
    apk add --no-cache \
        wget \
        build-base \
        ruby \
        ruby-dev \
        libffi-dev \
        libxml2-dev && \
    gem install --no-update-sources \
        rspec-its:1.3.0 hitimes:1.3.1 public_suffix:4.0.7 multipart-post:2.2.0 etc:1.3.0 bundler:2.3.26 chef:13.8.5 \
        faraday:2.8.1 minitar:0.12.1 berkshelf:6.3.1 \
        io-console:0.4.6 \
        webrick \
        json \
        --no-document && \
    gem cleanup

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

COPY --chown=onap:onap chef-solo $JETTY_FOLDER/chef-solo/
COPY --chown=onap:onap  chef-repo/cookbooks $JETTY_FOLDER/chef-solo/cookbooks/
ADD --chown=onap:onap WSSimulator*.war $JETTY_FOLDER/webapps/
COPY --chown=onap:onap startup.sh $JETTY_FOLDER/

RUN chmod 770 $JETTY_FOLDER/startup.sh

ENTRYPOINT [ "sh", "-c", "${JETTY_HOME}/startup.sh"]
