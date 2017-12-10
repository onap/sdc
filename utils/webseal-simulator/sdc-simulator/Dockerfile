FROM onap/base_sdc-jetty:1.2.0-SNAPSHOT-latest

COPY chef-solo /root/chef-solo/

COPY chef-repo/cookbooks /root/chef-solo/cookbooks/

RUN cp ${JETTY_HOME}/resources/log4j.properties ${JETTY_BASE}/resources/log4j.properties

ADD WSSimulator*.war                    	${JETTY_BASE}/webapps/

USER root

RUN chown -R jetty:jetty               		${JETTY_BASE}/webapps

COPY startup.sh /root/

RUN chmod 777 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
