FROM onap/integration-java11:7.1.0

USER root

# Install Chef
RUN set -ex && \
    apk update && \
    apk add --no-cache \
        build-base \
        ruby \
        ruby-dev \
        libffi-dev \
        libxml2-dev && \
    gem install \
        chef:13.8.5 \
        berkshelf:6.3.1 \
        io-console:0.4.6 \
        etc webrick \
        --no-document && \
    gem cleanup && \
    apk update

RUN cd /app
#Download jetty
RUN wget https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.4.31.v20200723/jetty-distribution-9.4.31.v20200723.tar.gz && \
        tar xvfz jetty-distribution-9.4.31.v20200723.tar.gz && \
        mv jetty-distribution-9.4.31.v20200723 jetty && \
        rm -rf jetty-distribution-9.4.31.v20200723.tar.gz

ENV JETTY_BASE=/app/jetty

COPY --chown=onap:onap chef-solo ${JETTY_BASE}/chef-solo/
COPY --chown=onap:onap chef-repo/cookbooks ${JETTY_BASE}/chef-solo/cookbooks/
ADD --chown=onap:onap onboarding-be-*.war    ${JETTY_BASE}/webapps/
ADD --chown=onap:onap api-docs.war           ${JETTY_BASE}/webapps/
COPY --chown=onap:onap startup.sh ${JETTY_BASE}/

RUN chmod 770 ${JETTY_BASE}/startup.sh

ENTRYPOINT [ "sh", "-c", "${JETTY_BASE}/startup.sh"]
