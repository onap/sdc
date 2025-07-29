FROM onap/policy-jdk-alpine:2.4.4

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
        wget \
        build-base \
        ruby \
        ruby-dev \
        libffi-dev \
        libxml2-dev && \
    gem install --no-update-sources \
        multi_json:1.15.0 \
        rspec-its:1.3.0 hitimes:1.3.1 public_suffix:4.0.7 multipart-post:2.2.0 etc:1.3.0 bundler:2.3.26 chef:13.8.5 \
        faraday:2.8.1 minitar:0.12.1 berkshelf:6.3.1 \
        io-console:0.4.6 \
        webrick \
        json \
        --no-document && \
    gem cleanup

USER sdc

COPY --chown=sdc:sdc init_keyspaces.cql /home/sdc/
COPY --chown=sdc:sdc init_schemas.cql /home/sdc/
COPY --chown=sdc:sdc upgrade-scripts /home/sdc/upgrade-scripts
COPY --chown=sdc:sdc startup.sh /home/sdc/

RUN chmod 770 /home/sdc/startup.sh
RUN chmod -R go+rwx /home/sdc
ENTRYPOINT [ "/home/sdc/startup.sh" ]

 