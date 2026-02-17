FROM onap/policy-jdk-alpine:2.4.4

USER root
RUN addgroup sdc
RUN adduser --gecos "sdc sdc,1,1,1" --disabled-password --ingroup sdc --shell /bin/sh sdc
USER sdc
RUN mkdir ~/.cassandra/ && \
    echo  '[cql]' > ~/.cassandra/cqlshrc  && \
    echo  'version=3.4.4' >> ~/.cassandra/cqlshrc
USER root

RUN set -ex && \
    for i in 1 2 3 4 5; do apk update && break || sleep 10; done && apk info > /dev/null && \
    pip3 install --upgrade 'pip<23' 'setuptools<66' wheel && \
    pip3 install cqlsh==6.1.0 && \
    command -v cqlsh && \
    mkdir ~/.cassandra/ && \
    echo  '[cql]' > ~/.cassandra/cqlshrc  && \
    echo  'version=3.4.4' >> ~/.cassandra/cqlshrc  && \
    apk add --no-cache wget

USER sdc

COPY --chown=sdc:sdc init_keyspaces.cql /home/sdc/
COPY --chown=sdc:sdc init_schemas.cql /home/sdc/
COPY --chown=sdc:sdc upgrade-scripts /home/sdc/upgrade-scripts
COPY --chown=sdc:sdc startup.sh /home/sdc/

RUN chmod 770 /home/sdc/startup.sh

ENTRYPOINT [ "/home/sdc/startup.sh" ]
