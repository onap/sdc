FROM cassandra:3.11.19

# Install necessary packages
RUN apt-get -o Acquire::Check-Valid-Until=false update && \
    apt-get -y --no-install-recommends install \
      apt-transport-https \
      curl \
      wget \
      perl \
      python3 \
      python3-pip \
      ntp && \
    apt-get -y autoremove && \
    pip3 install cqlsh==6.1.0 && \
    rm -rf "/opt/cassandra/bin/cqlsh*"

# Copy scripts or configuration files
RUN mkdir /root/scripts
COPY scripts /root/scripts
RUN chmod +x /root/scripts/*
RUN cp -pr /root/scripts/ready_probe.sh /var/lib/ready_probe.sh
RUN chmod +x /var/lib/ready_probe.sh

# Copy startup script
COPY startup.sh /root/

# Set executable permissions for the startup script
RUN chmod 770 /root/startup.sh

# Define the entrypoint for the container
ENTRYPOINT [ "/root/startup.sh" ]
