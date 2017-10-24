FROM openecomp/base_sdc-backend:1.0.0-SNAPSHOT-latest

COPY chef-solo /root/chef-solo/

COPY chef-repo/cookbooks/. /root/chef-solo/cookbooks/

ADD onboarding-be-*.war    ${JETTY_BASE}/webapps/

ADD catalog-be-*.war       ${JETTY_BASE}/webapps/

ADD api-docs.war           ${JETTY_BASE}/webapps/

USER root

RUN chown -R jetty:jetty   ${JETTY_BASE}/webapps

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
