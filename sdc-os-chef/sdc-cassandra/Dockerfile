FROM cassandra:3.11.12

RUN apt-get -o Acquire::Check-Valid-Until=false update && \
    apt-get -y --no-install-recommends install \
      apt-transport-https \
      curl \
      wget \
      perl \
      python \
      ntp && \
    apt-get -y autoremove && \
    curl -L https://omnitruck.chef.io/install.sh | bash -s -- -v 13.12.14

COPY chef-solo /root/chef-solo/
COPY chef-repo/cookbooks /root/chef-solo/cookbooks/
COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]