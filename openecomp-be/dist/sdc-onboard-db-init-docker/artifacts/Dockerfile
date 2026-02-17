FROM python:3.11-alpine

RUN addgroup sdc && \
    adduser --gecos "sdc sdc,1,1,1" --disabled-password --ingroup sdc --shell /bin/sh sdc

RUN apk add --no-cache wget && \
    pip install --no-cache-dir cqlsh==6.1.0

USER sdc

RUN mkdir ~/.cassandra/ && \
    echo '[cql]' > ~/.cassandra/cqlshrc && \
    echo 'version=3.4.4' >> ~/.cassandra/cqlshrc

USER root
RUN mkdir -p /root/.cassandra/ && \
    echo '[cql]' > /root/.cassandra/cqlshrc && \
    echo 'version=3.4.4' >> /root/.cassandra/cqlshrc

USER sdc

COPY --chown=sdc:sdc init_keyspaces.cql /home/sdc/
COPY --chown=sdc:sdc init_schemas.cql /home/sdc/
COPY --chown=sdc:sdc upgrade-scripts /home/sdc/upgrade-scripts
COPY --chown=sdc:sdc startup.sh /home/sdc/

RUN chmod 770 /home/sdc/startup.sh

ENTRYPOINT [ "/home/sdc/startup.sh" ]
