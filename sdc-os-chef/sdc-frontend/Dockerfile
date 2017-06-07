FROM jetty:9.3.15-jre8

RUN apt-get -y update
RUN apt-get -y install apt-utils
RUN apt-get -y install curl
RUN apt-get -y install vim

COPY chef-solo /root/chef-solo/
COPY chef-repo/cookbooks /root/chef-solo/cookbooks/

# install chef-solo
RUN curl -L https://omnitruck.chef.io/install.sh | bash

RUN cp ${JETTY_HOME}/resources/log4j.properties ${JETTY_BASE}/resources/log4j.properties

ADD onboarding*.war                    ${JETTY_BASE}/webapps/
ADD catalog-fe-*.war     ${JETTY_BASE}/webapps/
RUN chown -R jetty:jetty               ${JETTY_BASE}/webapps

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
