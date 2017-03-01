FROM jetty:9.3.15-jre8

RUN apt-get -y update
RUN apt-get -y install apt-utils
RUN apt-get -y install curl
RUN apt-get -y install vim





ADD ./../target/WSSimulator.war      ${JETTY_BASE}/webapps/
RUN chown -R jetty:jetty               ${JETTY_BASE}/webapps
ADD ./

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
