FROM onap/base_sdc-cqlsh:1.2.0-SNAPSHOT-latest

COPY create_activityspec_db.cql .
COPY start.sh .

RUN chmod 744 start.sh

ENTRYPOINT ["/start.sh"]