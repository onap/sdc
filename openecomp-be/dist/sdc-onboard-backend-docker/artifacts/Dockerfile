FROM onap/base_sdc-jetty:1.4.1

COPY chef-solo /root/chef-solo/

COPY chef-repo/cookbooks /root/chef-solo/cookbooks/

ADD --chown=jetty:jetty onboarding-be-*.war    ${JETTY_BASE}/webapps/

ADD --chown=jetty:jetty api-docs.war           ${JETTY_BASE}/webapps/

USER root

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
