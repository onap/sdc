FROM onap/integration-python:9.0.0

USER root

# Needed for pycurl
ENV PYCURL_SSL_LIBRARY=openssl

# Install packages only needed for building
RUN apk update && \
    apk add binutils jq libpng && \
    apk add --no-cache \
        libcurl && \
    apk add --no-cache --virtual .build-dependencies \
        libressl-dev \
        libffi-dev \
        libxml2-dev \
        build-base \
        curl-dev && \
    # needed libcurl to install correctly \
    python -m pip install --upgrade pip && \
    pip install 'pycurl==7.44.1' && \
    apk del .build-dependencies

ENV ONAP_LOG=/home/onap/logs
RUN mkdir $ONAP_LOG && chown onap:onap $ONAP_LOG

# user/group are the same as in integration/docker/onap-python base image
ENV user=onap group=onap

USER onap

# Copy scripts and install them
COPY --chown=onap:onap scripts /home/onap/scripts

RUN chmod -R a+rx /home/onap/scripts && \
    cd /home/onap/scripts && \
    pip install --user .

# Ensure ALL .local/bin scripts are accessible to any UID
RUN chmod -R a+rx /home/onap/.local && \
    chmod -R a+rx /home/onap/.local/bin && \
    chmod -R a+rx /home/onap

# Make sure PATH includes .local/bin
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
