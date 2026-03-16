FROM python:3.11-alpine

USER root

# Create onap user/group (previously provided by onap/integration-python base image)
RUN addgroup -S onap && adduser -S -G onap onap

# Needed for pycurl SSL backend selection
ENV PYCURL_SSL_LIBRARY=openssl

# Install runtime libcurl, build and install pycurl, then remove build deps
RUN apk add --no-cache libcurl && \
    apk add --no-cache --virtual .build-deps build-base curl-dev openssl-dev && \
    pip install --no-cache-dir pycurl==7.44.1 && \
    apk del .build-deps

ENV ONAP_LOG=/home/onap/logs
RUN mkdir -p $ONAP_LOG && chown onap:onap $ONAP_LOG

USER onap

# Copy and install Python sdcBePy package
COPY --chown=onap:onap scripts /home/onap/scripts
RUN cd /home/onap/scripts && \
    pip install --no-cache-dir --user .

ENV PATH=$PATH:/home/onap/.local/bin

# Copy remaining files
COPY --chown=onap:onap normatives.tar.gz /home/onap/
COPY --chown=onap:onap custom-scripts/ /home/onap/
COPY --chown=onap:onap startup.sh /home/onap/startup.sh

# Ensure all files are accessible to any UID (OpenShift compatibility)
RUN chmod -R a+rx /home/onap

WORKDIR /home/onap/

ENTRYPOINT [ "/home/onap/startup.sh" ]
