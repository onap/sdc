FROM onap/policy-jdk-alpine:4.0.0

USER root
RUN addgroup sdc
RUN adduser --gecos "sdc sdc,1,1,1" --disabled-password --ingroup sdc --shell /bin/sh sdc
USER sdc
RUN mkdir ~/.cassandra/ && \
    echo  '[cql]' > ~/.cassandra/cqlshrc  && \
    echo  'version=3.4.4' >> ~/.cassandra/cqlshrc
USER root

RUN apk update &&  \
    python3 -m pip install --upgrade pip && \
    pip3 install cqlsh==6.1.0 && \
    mkdir ~/.cassandra/ && \
    echo  '[cql]' > ~/.cassandra/cqlshrc  && \
    echo  'version=3.4.4' >> ~/.cassandra/cqlshrc  && \
    set -ex && \
    apk add --no-cache \
    make \
    gcc \
    ruby \
    ruby-dev \
    libffi-dev \
    libxml2-dev \
    build-base && \
    gem install --no-update-sources public_suffix:4.0.7 multipart-post:2.2.0 etc:1.3.0 bundler:2.3.26 chef:13.8.5 berkshelf:6.3.1 io-console:0.4.6 webrick --no-document && \
    gem cleanup

USER sdc
COPY --chown=sdc:sdc chef-solo /home/sdc/chef-solo/
COPY --chown=sdc:sdc chef-repo/cookbooks /home/sdc/chef-solo/cookbooks/
COPY --chown=sdc:sdc startup.sh /home/sdc/

RUN chmod 770 /home/sdc/startup.sh

ENTRYPOINT [ "/home/sdc/startup.sh" ]
