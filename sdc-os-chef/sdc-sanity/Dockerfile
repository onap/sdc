FROM onap/base_sdc-sanity:1.2.0-SNAPSHOT-latest

COPY chef-solo /root/chef-solo/

COPY chef-repo/cookbooks/. /root/chef-solo/cookbooks/


ENV TESTS_BASE /var/lib/tests
RUN mkdir -p "$TESTS_BASE"
WORKDIR $TESTS_BASE

ADD test-apis-ci-*.jar                        ${TESTS_BASE}/

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
