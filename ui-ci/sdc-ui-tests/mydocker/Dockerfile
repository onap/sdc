FROM onap/base_sdc-vnc:1.4.1

# install packages
RUN DEBIAN_FRONTEND=noninteractive \
    apt-get -y update \
    && \
    apt-get -y install \
        jq \
        openssl \
    && \
    apt-get -y clean \
    && apt install -y firefox \
    && mkdir -p /opt/geckodriver \
    && cd /opt/geckodriver && { curl -LJO https://github.com/mozilla/geckodriver/releases/download/v0.26.0/geckodriver-v0.26.0-linux64.tar.gz; cd -;} \
    && tar -C /opt/geckodriver/ -xvf /opt/geckodriver/geckodriver-v0.26.0-linux64.tar.gz

#COPY chef-solo /root/chef-solo/

#COPY chef-repo/cookbooks/. /root/chef-solo/cookbooks/


ENV TESTS_BASE /var/lib/tests
RUN mkdir -p "$TESTS_BASE"
WORKDIR $TESTS_BASE

#ADD ui-ci-*.jar                        ${TESTS_BASE}/

#COPY startup.sh /root/

#RUN chmod 770 /root/startup.sh

#ENTRYPOINT [ "/bin/bash" ]
