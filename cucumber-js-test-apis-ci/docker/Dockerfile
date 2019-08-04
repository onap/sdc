FROM node:8.16.0

ENV TESTS_BASE /var/lib/tests
USER root
RUN mkdir $TESTS_BASE
RUN mkdir $TESTS_BASE/environments
COPY cucumber $TESTS_BASE
COPY startup.sh .
RUN chmod 777 ./startup.sh
RUN chmod -R 777 $TESTS_BASE/node_modules/

ENTRYPOINT [ "./startup.sh" ]
