FROM jetty:alpine

EXPOSE 8080
EXPOSE 8443
EXPOSE 5005

ARG ARTIFACT_VERSION

# See https://hub.docker.com/_/jetty/
ARG WEBAPPS_DIR=${JETTY_BASE}/webapps/

COPY activity-spec-api-docs/target/activity-spec-api-docs-${ARTIFACT_VERSION}.war ${WEBAPPS_DIR}

COPY activity-spec-war/target/activity-spec-war-${ARTIFACT_VERSION}.war ${WEBAPPS_DIR}

COPY activity-spec-assembly/configuration.yaml .
COPY activity-spec-assembly/start.sh .

USER root

RUN chown jetty:jetty start.sh
RUN chmod 744 start.sh

ENTRYPOINT ["./start.sh"]