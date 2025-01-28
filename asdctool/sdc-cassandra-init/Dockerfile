<<<<<<< PATCH SET (b19277 Chef removal changes for asdc_tool)
FROM onap/policy-jdk-debian:2.0.2

# Create a new group and user
RUN addgroup sdc && \
    adduser --gecos "sdc sdc,1,1,1" --disabled-password --ingroup sdc --shell /bin/sh sdc

=======
FROM onap/policy-jdk-alpine:2.4.4
 
USER root
 
# Add 'sdc' user
RUN addgroup sdc && \
    adduser --gecos "sdc sdc,1,1,1" --disabled-password --ingroup sdc --shell /bin/sh sdc
 
>>>>>>> BASE      (92c86b Chef removal changes from catalog-fe)
USER sdc
<<<<<<< PATCH SET (b19277 Chef removal changes for asdc_tool)

# Create Cassandra configuration directory and file
RUN mkdir -p ~/.cassandra/ && \
    echo '[cql]' > ~/.cassandra/cqlshrc && \
    echo 'version=3.4.4' >> ~/.cassandra/cqlshrc

=======
 
# Configure CQLSH
RUN mkdir -p ~/.cassandra/ && \
    echo '[cql]' > ~/.cassandra/cqlshrc && \
    echo 'version=3.4.4' >> ~/.cassandra/cqlshrc
 
>>>>>>> BASE      (92c86b Chef removal changes from catalog-fe)
USER root
<<<<<<< PATCH SET (b19277 Chef removal changes for asdc_tool)

# Update package list and install necessary packages
RUN apt-get update --allow-releaseinfo-change && \
    apt-get purge python* -y && \
    apt-get install -y python3-pip && \
    python3 -m pip install --upgrade pip && \
    pip3 install cqlsh==6.1.0 && \
    mkdir -p ~/.cassandra/ && \
    echo '[cql]' > ~/.cassandra/cqlshrc && \
    echo 'version=3.4.4' >> ~/.cassandra/cqlshrc && \
    apt-get install -y \
    make \
    gcc \
    ruby \
    ruby-dev \
    libffi-dev \
    libxml2-dev \
    binutils && \
    apt-get clean

# Remove bash only if absolutely necessary
RUN apt-get remove bash -y --allow-remove-essential || true

=======
 
# Install dependencies
RUN apk update && apk add --no-cache \
    python3 py3-pip wget build-base ruby ruby-dev libffi-dev openssl-dev libxml2-dev
 
# Upgrade pip and install cqlsh
RUN python3 -m pip install --upgrade pip && pip3 install cqlsh==6.1.0
 
# Install required Ruby gems
RUN gem install --no-document \
    rspec-its:1.3.0 hitimes:1.3.1 public_suffix:5.1.1 multipart-post:2.4.1 etc:1.4.3 \
    bundler:2.4.22 mixlib-log:3.1.1 chef:14.15.6 faraday:2.8.1 minitar:0.12.1 berkshelf:7.0.10 \
    io-console:0.7.2 webrick json
 
RUN gem cleanup
 
# Copy required files
>>>>>>> BASE      (92c86b Chef removal changes from catalog-fe)
USER sdc
<<<<<<< PATCH SET (b19277 Chef removal changes for asdc_tool)

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

# Debug: Verify the script exists and is executable
RUN ls -l /home/sdc/scripts/pre-process_config.sh && \
    /bin/sh -c "chmod +x /home/sdc/scripts/pre-process_config.sh"

# Run pre-process_config.sh
RUN /bin/sh -c "/home/sdc/scripts/pre-process_config.sh" || \
    { echo "pre-process_config.sh failed"; exit 1; }

# Define entrypoint
ENTRYPOINT [ "sh", "-c", "/home/sdc/startup.sh" ]
=======
COPY --chown=sdc:sdc chef-solo /home/sdc/chef-solo/
COPY --chown=sdc:sdc chef-repo/cookbooks /home/sdc/chef-solo/cookbooks/
COPY --chown=sdc:sdc startup.sh /home/sdc/
 
# Set permissions
USER root
RUN chmod 770 /home/sdc/startup.sh
 
# Set entrypoint
USER sdc
ENTRYPOINT [ "/home/sdc/startup.sh" ]
>>>>>>> BASE      (92c86b Chef removal changes from catalog-fe)
