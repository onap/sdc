FROM onap/base_sdc-jetty:1.2.0-SNAPSHOT-latest

ADD onboarding-be-*.war    ${JETTY_BASE}/webapps/

ADD api-docs.war           ${JETTY_BASE}/webapps/

ADD onboarding_configuration.yaml ${JETTY_BASE}/config/onboarding-be/

ADD logback.xml ${JETTY_BASE}/config/onboarding-be/

ADD ready-probe.sh /var/lib/

USER root

RUN chmod 770 /var/lib/ready-probe.sh

RUN chown -R jetty:jetty   ${JETTY_BASE}/webapps

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
