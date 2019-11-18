FROM onap/base_sdc-cqlsh:1.5.0-SNAPSHOT-latest

COPY init_keyspaces.cql /root/

COPY init_schemas.cql /root/

COPY alter_tables.cql /root/

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]

