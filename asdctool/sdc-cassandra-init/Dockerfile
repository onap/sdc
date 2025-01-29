FROM onap/policy-jdk-alpine:2.4.4
 
USER root
 
# Add 'sdc' user
RUN addgroup sdc && \
    adduser --gecos "sdc sdc,1,1,1" --disabled-password --ingroup sdc --shell /bin/sh sdc
 
USER sdc
 
# Configure CQLSH
RUN mkdir -p ~/.cassandra/ && \
    echo '[cql]' > ~/.cassandra/cqlshrc && \
    echo 'version=3.4.4' >> ~/.cassandra/cqlshrc
 
USER root
 
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
USER sdc
COPY --chown=sdc:sdc chef-solo /home/sdc/chef-solo/
COPY --chown=sdc:sdc chef-repo/cookbooks /home/sdc/chef-solo/cookbooks/
COPY --chown=sdc:sdc startup.sh /home/sdc/
 
# Set permissions
USER root
RUN chmod 770 /home/sdc/startup.sh
 
# Set entrypoint
USER sdc
ENTRYPOINT [ "/home/sdc/startup.sh" ]