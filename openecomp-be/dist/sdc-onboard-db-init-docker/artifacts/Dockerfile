FROM onap/policy-jdk-alpine:2.4.4

USER root

RUN apk update && apk upgrade && apk add --no-cache openssl zlib expat apk-tools

# Create sdc user and group
RUN addgroup sdc && \
    adduser --gecos "sdc sdc,1,1,1" --disabled-password --ingroup sdc --shell /bin/sh sdc

#Install only necessary build/runtime packages temporarily
RUN apk add --no-cache --virtual .build-deps wget python3 py3-pip && \
    python3 -m pip install --no-cache-dir --upgrade pip && \
    pip3 install --no-cache-dir cqlsh==6.1.0 && \
    apk del .build-deps && \
    rm -rf /root/.cache/pip /var/cache/apk/*

# Switch to sdc user and setup Cassandra configuration
USER sdc
RUN mkdir -p ~/.cassandra/ && \
    echo '[cql]' > ~/.cassandra/cqlshrc && \
    echo 'version=3.4.4' >> ~/.cassandra/cqlshrc && \
    chmod 600 ~/.cassandra/cqlshrc


COPY --chown=sdc:sdc init_keyspaces.cql /home/sdc/
COPY --chown=sdc:sdc init_schemas.cql /home/sdc/
COPY --chown=sdc:sdc upgrade-scripts /home/sdc/upgrade-scripts
COPY --chown=sdc:sdc startup.sh /home/sdc/

RUN chmod 770 /home/sdc/startup.sh

ENTRYPOINT [ "/home/sdc/startup.sh" ]
