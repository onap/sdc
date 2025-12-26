# Base Cassandra image
FROM cassandra:3.11.15

# Create a non-root user for runtime
RUN useradd -ms /bin/sh sdc

# Switch to root for installations
USER root

# Install only necessary packages
RUN apt-get -o Acquire::Check-Valid-Until=false update && \
    apt-get install -y --no-install-recommends \
        python3 python3-pip curl wget perl ntp apt-transport-https && \
    pip3 install --no-cache-dir cqlsh==6.1.0 && \
    rm -rf /var/lib/apt/lists/* /root/.cache/pip

# Copy scripts or configuration files
RUN mkdir -p /opt/scripts && chown -R sdc:sdc /opt/scripts
COPY scripts/ /opt/scripts/
RUN chmod 770 /opt/scripts/*

# Setup ready probe
RUN cp -pr /opt/scripts/ready_probe.sh /var/lib/ready_probe.sh && \
    chmod 770 /var/lib/ready_probe.sh && \
    chown sdc:sdc /var/lib/ready_probe.sh

# Copy startup script
COPY startup.sh /opt/startup.sh
RUN chmod 770 /opt/startup.sh && chown sdc:sdc /opt/startup.sh

# Switch to sdc non-root user for runtime
USER sdc
WORKDIR /var/lib/cassandra

ENTRYPOINT [ "/opt/startup.sh" ]
