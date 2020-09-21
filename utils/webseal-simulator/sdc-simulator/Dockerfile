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

COPY chef-solo /root/chef-solo/
COPY chef-repo/cookbooks /root/chef-solo/cookbooks/
ADD WSSimulator*.war                    	${JETTY_BASE}/webapps/
RUN chown -R onap:onap               		${JETTY_BASE}/webapps
COPY startup.sh ${JETTY_BASE}/
RUN chmod 777 ${JETTY_BASE}/startup.sh

ENTRYPOINT [ "sh", "-c", "${JETTY_BASE}/startup.sh"]
