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
RUN chmod -R 777 $ONAP_LOG

# user/group are the same as in integration/docker/onap-python base image
ENV user=onap group=onap

USER onap

# user/group args are inherited from base_sdc-python base image
COPY --chown=onap:onap scripts /home/onap/scripts

RUN chmod 770 -R /home/onap/scripts && \
    cd /home/onap/scripts && \
    python setup.py install --user

ENV PATH=$PATH:/home/onap/.local/bin

COPY --chown=onap:onap normatives.tar.gz /home/onap/
COPY --chown=onap:onap custom-scripts/create_consumer_and_user.sh /home/onap/create_consumer_and_user.sh
RUN chmod +x /home/onap/create_consumer_and_user.sh;
COPY --chown=onap:onap custom-scripts/check_backend.sh /home/onap/check_backend.sh
RUN chmod +x /home/onap/check_backend.sh
COPY --chown=onap:onap custom-scripts/import_normatives.sh /home/onap/import_normatives.sh
RUN chmod +x /home/onap/import_normatives.sh

COPY --chown=onap:onap startup.sh /home/onap/

RUN chmod 770 /home/onap/startup.sh

WORKDIR /home/onap/

ENTRYPOINT [ "/home/onap/startup.sh" ]

RUN ls -ld /home/onap/logs && id onap