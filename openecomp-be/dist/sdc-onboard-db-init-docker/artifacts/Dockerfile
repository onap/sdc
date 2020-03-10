FROM onap/base_sdc-cqlsh:1.6.0-SNAPSHOT-latest

COPY --chown=sdc:sdc init_keyspaces.cql /home/sdc/

COPY --chown=sdc:sdc init_schemas.cql /home/sdc/

COPY --chown=sdc:sdc alter_tables.cql /home/sdc/ 

COPY --chown=sdc:sdc startup.sh /home/sdc/ 

RUN chmod 770 /home/sdc/startup.sh

ENTRYPOINT [ "/home/sdc/startup.sh" ]
