# The build stage is necessary here to avoid including the tar file
# in a separate image layer
FROM eclipse-temurin:11-jre-jammy AS builder
COPY sdctool.tar /tmp/sdctool.tar
RUN mkdir -p /opt && \
    tar -xf /tmp/sdctool.tar -C /opt && \
    rm /tmp/sdctool.tar

# Final image
FROM eclipse-temurin:11-jre-jammy

USER root
RUN groupadd -r sdc && useradd -r -g sdc -m sdc

RUN apt-get update && \
    apt-get install -y --no-install-recommends python3-pip && \
    pip3 install --no-cache-dir cqlsh==6.1.0 && \
    apt-get purge -y --auto-remove python3-pip && \
    rm -rf /var/lib/apt/lists/* /root/.cache/pip

USER sdc
WORKDIR /home/sdc

COPY --from=builder --chown=sdc:sdc /opt/sdctool ./sdctool
COPY --chown=sdc:sdc scripts ./scripts
COPY --chown=sdc:sdc startup.sh ./

RUN mkdir -p tools && \
    cp -pr sdctool/tools/* tools/ && \
    cp scripts/janusgraph.properties sdctool/config/ && \
    cp scripts/configuration.yaml sdctool/config/ && \
    find /home/sdc -type f -name "*.sh" -exec chmod 750 {} \;

ENTRYPOINT ["/bin/sh", "/home/sdc/startup.sh"]
