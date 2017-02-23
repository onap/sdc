FROM kibana:4.3.3

RUN apt-get -y update
RUN apt-get -y install curl
RUN apt-get -y install vim

COPY chef-solo /root/chef-solo/
COPY chef-repo/cookbooks/. /root/chef-solo/cookbooks/


# install chef-solo
RUN curl -L https://omnitruck.chef.io/install.sh | bash



COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
