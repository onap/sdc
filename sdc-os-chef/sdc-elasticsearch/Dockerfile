FROM elasticsearch:2.1.2

RUN apt-get -y update
RUN apt-get -y install apt-utils
RUN apt-get -y install curl
RUN apt-get -y install vim
RUN mkdir -p /var/chef/nodes

COPY chef-solo /root/chef-solo/
COPY chef-repo/cookbooks /root/chef-solo/cookbooks/

ENV BASE_NEXUS zl999y:ChangeMe@10.208.197.75:8443/repository/maven-public/org/openecomp/sdc

# install chef-solo
RUN curl -L https://www.opscode.com/chef/install.sh | bash

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
