FROM onap/policy-jdk-debian:2.0.1

#RUN addgroup --gid 1000 sdc
RUN addgroup sdc
#RUN adduser --gecos "sdc sdc,1,1,1" -u 1000 --disabled-password --ingroup sdc --shell /bin/sh sdc
RUN adduser --gecos "sdc sdc,1,1,1" --disabled-password --ingroup sdc --shell /bin/sh sdc
USER sdc
RUN mkdir ~/.cassandra/ && \
    echo  '[cql]' > ~/.cassandra/cqlshrc  && \
    echo  'version=3.4.4' >> ~/.cassandra/cqlshrc
USER root

RUN apt-get update && apt-get install -y python-pip && \
    pip install cqlsh && \
    mkdir ~/.cassandra/ && \
    echo  '[cql]' > ~/.cassandra/cqlshrc  && \
    echo  'version=3.4.4' >> ~/.cassandra/cqlshrc  && \
    set -ex && \
    pip install cqlsh && \
    apt-get install -y \
    make \
    gcc \
    ruby \
    ruby-dev \
    libffi-dev \
    libxml2-dev && \
    gem install chef:13.8.5 berkshelf:6.3.1 io-console:0.4.6 etc webrick --no-document && \
    apt-get update -y && apt-get remove bash -y --allow-remove-essential && \
    apt-get install -y binutils && apt-get clean && gem cleanup

USER sdc

COPY --chown=sdc:sdc init_keyspaces.cql /home/sdc/
COPY --chown=sdc:sdc init_schemas.cql /home/sdc/
COPY --chown=sdc:sdc alter_tables.cql /home/sdc/
COPY --chown=sdc:sdc startup.sh /home/sdc/ 

RUN chmod 770 /home/sdc/startup.sh

ENTRYPOINT [ "/home/sdc/startup.sh" ]
