FROM onap/base_sdc-jetty:1.4.1

COPY chef-solo /root/chef-solo/

COPY chef-repo/cookbooks /root/chef-solo/cookbooks/

ADD --chown=jetty:jetty onboarding*.war         ${JETTY_BASE}/webapps/

ADD --chown=jetty:jetty catalog-fe-*.war        ${JETTY_BASE}/webapps/

USER root

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
