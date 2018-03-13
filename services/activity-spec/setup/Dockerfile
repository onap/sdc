#FROM openjdk:8-jdk-alpine

#RUN  apk add --no-cache \
#  py-pip

#RUN pip install cqlsh==5.0.1

FROM cassandra:2.1.9

ENV CASSANDRA_PORT=9042

COPY create_activityspec_db.cql .
COPY start.sh .

RUN chmod 744 start.sh

ENTRYPOINT ["/start.sh"]