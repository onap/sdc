FROM onap/policy-jdk-debian:2.0.2

RUN addgroup sdc
RUN adduser --gecos "sdc sdc,1,1,1" --disabled-password --ingroup sdc --shell /bin/sh sdc
USER sdc
RUN mkdir ~/.cassandra/ && \
    echo  '[cql]' > ~/.cassandra/cqlshrc  && \
    echo  'version=3.4.4' >> ~/.cassandra/cqlshrc
USER root

RUN apt-get update --allow-releaseinfo-change && apt-get install -y python-pip && \
    python -m pip install --upgrade pip \
    pip install cqlsh==6.0.0 && \
    mkdir ~/.cassandra/ && \
    echo  '[cql]' > ~/.cassandra/cqlshrc  && \
    echo  'version=3.4.4' >> ~/.cassandra/cqlshrc  && \
    set -ex && \
    apt-get install -y \
    make \
    gcc \
    ruby \
    ruby-dev \
    libffi-dev \
    libxml2-dev && \
    gem install multipart-post -v 2.2.0 --no-document && \
    gem install chef:13.8.5 berkshelf:6.3.1 io-console:0.4.6 etc webrick --no-document && \
    apt-get update -y && apt-get remove bash -y --allow-remove-essential && \
    apt-get install -y binutils && apt-get clean && gem cleanup

USER sdc

COPY --chown=sdc:sdc init_keyspaces.cql /home/sdc/
COPY --chown=sdc:sdc init_schemas.cql /home/sdc/
COPY --chown=sdc:sdc upgrade-scripts /home/sdc/upgrade-scripts
COPY --chown=sdc:sdc startup.sh /home/sdc/

RUN chmod 770 /home/sdc/startup.sh

ENTRYPOINT [ "/home/sdc/startup.sh" ]
