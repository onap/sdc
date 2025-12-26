FROM onap/integration-python:9.0.0

USER root

# Needed for pycurl
ENV PYCURL_SSL_LIBRARY=openssl

RUN apk update && apk upgrade && apk add --no-cache expat apk-tools

# Install only runtime packages and build dependencies temporarily
RUN apk update && \
    apk add --no-cache libcurl jq libpng python3 py3-pip && \
    apk add --no-cache --virtual .build-deps \
        libressl-dev \
        libffi-dev \
        libxml2-dev \
        build-base \
        curl-dev && \
    pip install --no-cache-dir pycurl==7.44.1 && \
    apk del .build-deps

ENV ONAP_LOG=/home/onap/logs
RUN mkdir -p $ONAP_LOG && chown onap:onap $ONAP_LOG

# user/group are the same as in integration/docker/onap-python base image
ENV user=onap group=onap

USER onap

# Copy scripts and install them under sdc user
COPY --chown=onap:onap scripts /home/onap/scripts

RUN chmod -R a+rx /home/onap/scripts && \
   cd /home/onap/scripts && \
    pip install --user .

# Ensure ALL .local/bin scripts are accessible to any UID
RUN chmod -R a+rx /home/onap/.local && \
    chmod -R a+rx /home/onap/.local/bin && \
    chmod -R a+rx /home/onap

# Add .local/bin to PATH and Python site-packages to PYTHONPATH
ENV PATH=$PATH:/home/onap/.local/bin
ENV PYTHONPATH=/home/onap/.local/lib/python3.9/site-packages:$PYTHONPATH

# Copy other required files
COPY --chown=onap:onap normatives.tar.gz /home/onap/
COPY --chown=onap:onap custom-scripts/create_consumer_and_user.sh /home/onap/create_consumer_and_user.sh
COPY --chown=onap:onap custom-scripts/check_backend.sh /home/onap/check_backend.sh
COPY --chown=onap:onap custom-scripts/import_normatives.sh /home/onap/import_normatives.sh
COPY --chown=onap:onap startup.sh /home/onap/startup.sh

RUN chmod a+rx /home/onap/*.sh

WORKDIR /home/onap/

ENTRYPOINT [ "/home/onap/startup.sh" ]
