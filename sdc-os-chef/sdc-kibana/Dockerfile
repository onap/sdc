FROM openecomp/base_sdc-kibana:v1.0.0

COPY chef-solo /root/chef-solo/

COPY chef-repo/cookbooks/. /root/chef-solo/cookbooks/

COPY startup.sh /root/

RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
