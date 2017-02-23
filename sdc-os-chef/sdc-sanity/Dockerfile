FROM ubuntu
RUN apt-get -y update && apt-get -y install --no-install-recommends apt-utils
RUN apt-get -y install curl
RUN apt-get -y install vim
RUN apt-get -y install default-jre && apt-get -y install openjdk-8-jdk
RUN update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java


COPY chef-solo /root/chef-solo/
COPY chef-repo/cookbooks/. /root/chef-solo/cookbooks/


# install chef-solo
RUN curl -L https://omnitruck.chef.io/install.sh | bash

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
