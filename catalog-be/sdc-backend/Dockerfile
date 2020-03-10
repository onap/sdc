FROM onap/base_sdc-jetty:1.6.0
 
COPY --chown=jetty:jetty chef-solo ${JETTY_BASE}/chef-solo/
COPY --chown=jetty:jetty chef-repo/cookbooks/. ${JETTY_BASE}/chef-solo/cookbooks/

ADD --chown=jetty:jetty catalog-be-*.war       ${JETTY_BASE}/webappwar/
ADD --chown=jetty:jetty context.xml       ${JETTY_BASE}/webapps/

COPY --chown=jetty:jetty startup.sh ${JETTY_BASE}/ 

RUN chmod 770 ${JETTY_BASE}/startup.sh

ENTRYPOINT ${JETTY_BASE}/startup.sh
