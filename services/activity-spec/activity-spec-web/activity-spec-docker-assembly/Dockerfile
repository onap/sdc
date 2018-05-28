FROM jetty:9.4.9-alpine

EXPOSE 8080
EXPOSE 8443
EXPOSE 5005

USER root

RUN mkdir -p /etc/onap/activity-spec/be
RUN mkdir -p /var/log/ONAP/activity-spec/be

ENV CONFIG_FILES_DIR /etc/onap/activity-spec/be
ENV LOGS_DIR /var/log/ONAP/activity-spec/be

ADD activity-spec-api-docs-*.war ${JETTY_BASE}/webapps/
ADD activity-spec-war-*.war ${JETTY_BASE}/webapps/

COPY start.sh .
COPY logback.xml ${CONFIG_FILES_DIR}

RUN chown -R jetty:jetty ${JETTY_BASE}/webapps
RUN chmod 744 start.sh

ENTRYPOINT [ "./start.sh" ]