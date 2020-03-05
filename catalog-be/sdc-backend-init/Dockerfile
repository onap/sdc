FROM python:3.8-alpine

# Needed for pycurl
ENV PYCURL_SSL_LIBRARY=openssl

# Install packages only needed for building
RUN apk update && \
    apk add binutils bash musl-dev curl curl-dev && \
    apk add --no-cache jq libressl-dev ruby=2.6.5-r2 ruby-dev libffi-dev libxml2-dev && \
    apk add --no-cache --virtual .build-dependencies build-base curl-dev && \
    gem install chef:13.8.5 berkshelf:6.3.1 io-console:0.4.6 etc webrick --no-document

COPY chef-solo /root/chef-solo/

COPY chef-repo/cookbooks /root/chef-solo/cookbooks/

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

COPY scripts /root/scripts
RUN chmod 770 -R /root/scripts && \
    cd /root/scripts && \
    python setup.py install

ENTRYPOINT [ "/root/startup.sh" ]
