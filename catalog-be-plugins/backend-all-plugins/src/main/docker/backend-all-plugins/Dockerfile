FROM ${docker.namespace}/sdc-backend:latest

ARG JETTY_USER=onap

COPY --chown=${JETTY_USER}:${JETTY_USER} onap-sdc-backend-all-plugins/etsi-nfv-nsd-csar-plugin.jar ${JETTY_BASE}/plugins/
