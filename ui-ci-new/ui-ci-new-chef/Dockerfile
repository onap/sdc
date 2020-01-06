FROM onap/base_sdc-vnc:1.4.1

ENV TESTS_BASE /var/lib/tests
ENV TESTS_BASE /var/lib/tests
# install packages
RUN DEBIAN_FRONTEND=noninteractive \
    apt-get -y update \
    &&  apt-get -y install \
        jq \
        openssl \
        firefox \
    && apt-get -y clean \
    && mkdir -p /opt/geckodriver \
    && cd /opt/geckodriver && { curl -LJO https://github.com/mozilla/geckodriver/releases/download/v0.26.0/geckodriver-v0.26.0-linux64.tar.gz; cd -;} \
    && tar -C /opt/geckodriver/ -xvf /opt/geckodriver/geckodriver-v0.26.0-linux64.tar.gz \
    && mkdir -p "$TESTS_BASE"

COPY chef-solo /root/chef-solo/
COPY chef-repo/cookbooks/. /root/chef-solo/cookbooks/

WORKDIR $TESTS_BASE

ADD ui-ci-new-*.jar ${TESTS_BASE}/
COPY startup.sh /root/
RUN chmod 770 /root/startup.sh
ENTRYPOINT [ "/root/startup.sh" ]
