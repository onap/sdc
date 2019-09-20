FROM onap/base_sdc-vnc:1.4.1

# install packages
RUN DEBIAN_FRONTEND=noninteractive \
    apt-get -y update \
    && \
    apt-get -y install \
        jq \
        openssl \
    && \
    apt-get -y clean

COPY chef-solo /root/chef-solo/

COPY chef-repo/cookbooks/. /root/chef-solo/cookbooks/


ENV TESTS_BASE /var/lib/tests
RUN mkdir -p "$TESTS_BASE"
WORKDIR $TESTS_BASE

ADD ui-ci-*.jar                        ${TESTS_BASE}/

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
