FROM jetty:9.3-jre8

RUN apt-get -y update
RUN apt-get -y install apt-utils
RUN apt-get -y install curl
RUN apt-get -y install vim
RUN apt-get -y install jq
RUN apt-get -y install python libssl-dev libcurl4-openssl-dev python-dev gcc

COPY chef-solo /root/chef-solo/
COPY chef-repo/cookbooks/. /root/chef-solo/cookbooks/

RUN python /root/chef-solo/cookbooks/sdc-normatives/files/default/get-pip.py
RUN pip install pycurl

# install chef-solo
RUN curl -L https://omnitruck.chef.io/install.sh | bash

ADD onboarding-be-*.war                        ${JETTY_BASE}/webapps/
ADD catalog-be-*.war                           ${JETTY_BASE}/webapps/
ADD api-docs.war	                           ${JETTY_BASE}/webapps/
RUN chown -R jetty:jetty                       ${JETTY_BASE}/webapps

COPY startup.sh /root/
RUN chmod 770 /root/startup.sh
ENTRYPOINT [ "/root/startup.sh" ]
