FROM onap/integration-python:9.1.0

USER root

# Needed for pycurl
ENV PYCURL_SSL_LIBRARY=openssl

# Install packages only needed for building
RUN apk update && \
    apk add binutils jq libpng && \
    apk add --no-cache \
        libcurl \
        ruby && \
    apk add --no-cache --virtual .build-dependencies \
        libressl-dev \
        ruby-dev \
        libffi-dev \
        libxml2-dev \
        build-base \
        curl-dev && \
    # needed libcurl to install correctly \
    python -m pip install --upgrade pip && \
    pip install 'pycurl==7.44.1' &&  \
    set -ex && \
    gem install --no-update-sources \
        rspec-its:1.3.0 hitimes:1.3.1 public_suffix:4.0.7 multipart-post:2.2.0 etc:1.3.0 bundler:2.3.26 chef:13.8.5 \
        faraday:2.8.1 minitar:0.12.1 berkshelf:6.3.1 \
        io-console:0.4.6 \
        webrick \
        json \
        --no-document && \
    apk del .build-dependencies && \
    gem cleanup

ENV ONAP_LOG=/home/onap/logs
RUN mkdir $ONAP_LOG && chown onap:onap $ONAP_LOG

# user/group are the same as in integration/docker/onap-python base image
ENV user=onap group=onap

USER onap

# user/group args are inherited from base_sdc-python base image
COPY --chown=onap:onap scripts /home/onap/scripts

RUN chmod 770 -R /home/onap/scripts && \
    cd /home/onap/scripts && \
    python setup.py install --user

ENV PATH=$PATH:/home/onap/.local/bin

COPY --chown=onap:onap chef-solo /home/onap/chef-solo/
COPY --chown=onap:onap chef-repo/cookbooks /home/onap/chef-solo/cookbooks/
COPY --chown=onap:onap startup.sh /home/onap/

RUN chmod 770 /home/onap/startup.sh

WORKDIR /home/onap/

ENTRYPOINT /home/${user}/startup.sh
