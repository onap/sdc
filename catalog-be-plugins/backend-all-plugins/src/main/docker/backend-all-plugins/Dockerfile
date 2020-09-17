FROM onap/sdc-backend:latest

COPY --chown=jetty:jetty onap-sdc-backend-all-plugins/etsi-nfv-nsd-csar-plugin.jar       ${JETTY_BASE}/plugins/
RUN ls -latr ${JETTY_BASE}/plugins/