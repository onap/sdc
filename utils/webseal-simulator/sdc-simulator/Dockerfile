FROM onap/base_sdc-jetty:1.2.0-SNAPSHOT-latest
USER root

COPY chef-solo /root/chef-solo/
COPY chef-repo/cookbooks /root/chef-solo/cookbooks/
RUN cp ${JETTY_HOME}/resources/log4j.properties ${JETTY_BASE}/resources/log4j.properties
RUN mkdir -p ${JETTY_BASE}/config

ADD WSSimulator*.war ${JETTY_BASE}/webapps/

COPY startup.sh /root/

ENTRYPOINT [ "/root/startup.sh" ]
