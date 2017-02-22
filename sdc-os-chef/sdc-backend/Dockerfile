FROM jetty:9.3.15-jre8

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
#RUN curl -L http://www.opscode.com/chef/install.sh | bash
COPY chef-install.sh /root/
RUN chmod 770 /root/chef-install.sh
RUN bash /root/chef-install.sh

ADD onboarding-be-__SDC-RELEASE__.war          ${JETTY_BASE}/webapps/
ADD catalog-be-__SDC-RELEASE__.war             ${JETTY_BASE}/webapps/
RUN chown -R jetty:jetty                       ${JETTY_BASE}/webapps

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
