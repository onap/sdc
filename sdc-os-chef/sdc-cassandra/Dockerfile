FROM onap/base_sdc-cassandra:1.2.0-SNAPSHOT-latest

COPY chef-solo /root/chef-solo/

COPY chef-repo/cookbooks /root/chef-solo/cookbooks/

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
