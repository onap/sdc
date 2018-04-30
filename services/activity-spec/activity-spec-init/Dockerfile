FROM alpine:3.7

RUN apk add --no-cache 'python<3' py-pip && pip install cqlsh==4.0.1

COPY create_activityspec_db.cql .
COPY start.sh .

RUN chmod 744 start.sh

ENTRYPOINT ["./start.sh"]