FROM onap/integration-java11:7.1.0

USER onap
RUN mkdir ~/.cassandra/ && \
    echo  '[cql]' > ~/.cassandra/cqlshrc  && \
    echo  'version=3.4.4' >> ~/.cassandra/cqlshrc

USER root
RUN apk update && apk add --no-cache py-pip && \
    pip install cqlsh==5.0.4 && \
    mkdir ~/.cassandra/ && \
    echo  '[cql]' > ~/.cassandra/cqlshrc  && \
    echo  'version=3.4.4' >> ~/.cassandra/cqlshrc  && \
    set -ex && \
    pip install cqlsh && \
    apk add --no-cache \
    build-base \
    ruby \
    ruby-dev \
    libffi-dev \
    libxml2-dev && \
    gem install chef:13.8.5 berkshelf:6.3.1 io-console:0.4.6 etc webrick --no-document && \
    apk update && \
    apk add --no-cache binutils && gem cleanup
USER onap

COPY --chown=onap:onap chef-solo /home/sdc/chef-solo/
COPY --chown=onap:onap chef-repo/cookbooks /home/sdc/chef-solo/cookbooks/
COPY --chown=onap:onap startup.sh /home/sdc/

RUN chmod 770 /home/sdc/startup.sh

ENTRYPOINT [ "/home/sdc/startup.sh" ]