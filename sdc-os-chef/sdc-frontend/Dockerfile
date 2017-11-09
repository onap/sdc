FROM openecomp/base_sdc-frontend:v1.0.0

COPY chef-solo /root/chef-solo/

COPY chef-repo/cookbooks /root/chef-solo/cookbooks/

RUN cp ${JETTY_HOME}/resources/log4j.properties ${JETTY_BASE}/resources/log4j.properties

ADD onboarding*.war         ${JETTY_BASE}/webapps/

ADD catalog-fe-*.war        ${JETTY_BASE}/webapps/

USER root

RUN chown -R jetty:jetty    ${JETTY_BASE}/webapps

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
