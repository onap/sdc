FROM onap/policy-jdk-debian:2.0.2
 
# Create a new group and user
RUN addgroup sdc && \
    adduser --gecos "sdc sdc,1,1,1" --disabled-password --ingroup sdc --shell /bin/sh sdc
 
USER sdc
 
# Create Cassandra configuration directory and file
RUN mkdir ~/.cassandra/ && \
    echo '[cql]' > ~/.cassandra/cqlshrc && \
    echo 'version=3.4.4' >> ~/.cassandra/cqlshrc
 
USER root
 
# Update package list and install necessary packages
RUN apt-get update --allow-releaseinfo-change && \
    apt-get purge python* -y && \
    apt-get install -y python3-pip && \
    python3 -m pip install --upgrade pip && \
    pip3 install cqlsh==6.1.0 && \
    mkdir ~/.cassandra/ && \
    echo '[cql]' > ~/.cassandra/cqlshrc && \
    echo 'version=3.4.4' >> ~/.cassandra/cqlshrc && \
    set -ex && \
    apt-get install -y \
    make \
    gcc \
    ruby \
    ruby-dev \
    libffi-dev \
    libxml2-dev && \
    apt-get update -y && \
    apt-get remove bash -y --allow-remove-essential && \
    apt-get install -y binutils && apt-get clean
 
USER sdc
 
# Copy sdctool tar
COPY --chown=sdc:sdc sdctool.tar /tmp/sdctool.tar
 
RUN tar -xvf /tmp/sdctool.tar -C /tmp && \
    rm /tmp/sdctool.tar && \
    mkdir /tmp/tools && \
    cp -pr /tmp/sdctool/tools/* /tmp/tools && \
    chmod +x /tmp/sdctool/scripts/* && \
    chmod +x /tmp/tools/build/scripts/* && \
    chmod +x /tmp/tools/scripts/* && \
    chown -R sdc:sdc /tmp/tools/build/scripts/*
