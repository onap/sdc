FROM node:8.16.0


ENV TESTS_BASE /var/lib/tests
USER root
RUN mkdir $TESTS_BASE
COPY cucumber $TESTS_BASE 
COPY startup.sh $TESTS_BASE 
RUN chmod 777 $TESTS_BASE/startup.sh

ENTRYPOINT [ "/var/lib/tests/startup.sh" ]
