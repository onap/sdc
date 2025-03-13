FROM eclipse-temurin:11-jre-jammy

# Create a new group and user
RUN addgroup sdc && \
    adduser --gecos "sdc sdc,1,1,1" --disabled-password --ingroup sdc --shell /bin/sh sdc

USER sdc

# Create Cassandra configuration directory and file
RUN mkdir -p ~/.cassandra/ && \
    echo '[cql]' > ~/.cassandra/cqlshrc && \
    echo 'version=3.4.4' >> ~/.cassandra/cqlshrc

USER root

# Update package list and install necessary packages
RUN apt-get update --allow-releaseinfo-change && \
    apt-get purge python* -y && \
    apt-get install -y python3-pip && \
    python3 -m pip install --upgrade pip && \
    pip3 install --no-cache-dir cqlsh==6.1.0 && \
    mkdir -p ~/.cassandra/ && \
    echo '[cql]' > ~/.cassandra/cqlshrc && \
    echo 'version=3.4.4' >> ~/.cassandra/cqlshrc && \
    apt-get install -y \
    libffi-dev \
    libxml2-dev && \
    apt-get clean

# Remove bash only if absolutely necessary
RUN apt-get remove bash -y --allow-remove-essential || true

USER sdc

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

# Define entrypoint
ENTRYPOINT [ "sh", "-c", "/home/sdc/startup.sh" ]
