FROM onap/base_sdc-jetty:1.3.0-SNAPSHOT-latest

EXPOSE 8080
EXPOSE 8443
EXPOSE 5005

COPY chef-solo /root/chef-solo/

COPY chef-repo/cookbooks/. /root/chef-solo/cookbooks/

ADD activity-spec-api-docs-*.war ${JETTY_BASE}/webapps/
ADD activity-spec-war-*.war ${JETTY_BASE}/webapps/

USER root

RUN chown -R jetty:jetty   ${JETTY_BASE}/webapps

COPY start.sh /root/

RUN chmod 770 /root/start.sh

ENTRYPOINT [ "/root/start.sh" ]
