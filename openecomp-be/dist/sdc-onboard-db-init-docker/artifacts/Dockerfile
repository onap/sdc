FROM onap/base_sdc-cqlsh:1.3.0

COPY init_keyspaces.cql /root/

COPY init_schemas.cql /root/

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]

