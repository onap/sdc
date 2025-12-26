# Base image
FROM eclipse-temurin:11-jre-jammy

# Create a new group and user
RUN addgroup --system sdc && \
    adduser --system --ingroup sdc --shell /bin/sh sdc

USER sdc
WORKDIR /home/sdc

# Create Cassandra configuration directory and file
RUN mkdir -p ~/.cassandra/ && \
    echo '[cql]' > ~/.cassandra/cqlshrc && \
    echo 'version=3.4.4' >> ~/.cassandra/cqlshrc && \
    chmod 600 ~/.cassandra/cqlshrc

# Switch to root to install necessary packages
USER root
RUN apt-get update --allow-releaseinfo-change && \
    apt-get install -y --no-install-recommends \
        python3-pip \
        libffi-dev \
        libxml2-dev \
        curl \
        wget \
        perl \
        ntp \
        apt-transport-https && \
    python3 -m pip install --upgrade pip && \
    pip3 install --no-cache-dir cqlsh==6.1.0 && \
    apt-get purge -y python3-dev python3-apt && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /root/.cache/pip

# Switch back to system user for runtime
USER sdc
WORKDIR /home/sdc

# Copy sdctool tar and startup
COPY --chown=sdc:sdc sdctool.tar /home/sdc/sdctool.tar
COPY --chown=sdc:sdc scripts /home/sdc/scripts
COPY --chown=sdc:sdc startup.sh /home/sdc

# Extract and prepare sdctool
RUN tar -xvf /home/sdc/sdctool.tar -C /home/sdc && \
    rm /home/sdc/sdctool.tar && \
    mkdir -p /home/sdc/tools && \
    cp -pr /home/sdc/sdctool/tools/* /home/sdc/tools && \
    chmod +x /home/sdc/sdctool/scripts/* && \
    chmod +x /home/sdc/tools/build/scripts/* && \
    chmod +x /home/sdc/tools/scripts/* && \
    chown -R sdc:sdc /home/sdc/tools/build/scripts/* && \
    chmod +x /home/sdc/startup.sh && \
    chmod +x /home/sdc/scripts/* && \
    cp -r /home/sdc/scripts/janusgraph.properties /home/sdc/sdctool/config && \
    cp -r /home/sdc/scripts/configuration.yaml /home/sdc/sdctool/config

# Ensure all scripts are executable and owned by sdc
RUN find /home/sdc -type f -name "*.sh" -exec chmod 750 {} \; && \
    chown -R sdc:sdc /home/sdc


ENTRYPOINT ["/bin/sh", "/home/sdc/startup.sh"]
