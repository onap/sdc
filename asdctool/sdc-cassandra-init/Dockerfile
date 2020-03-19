FROM onap/base_sdc-cqlsh:1.6.1

COPY --chown=sdc:sdc chef-solo /home/sdc/chef-solo/

COPY --chown=sdc:sdc chef-repo/cookbooks /home/sdc/chef-solo/cookbooks/

COPY --chown=sdc:sdc startup.sh /home/sdc/

RUN chmod 770 /home/sdc/startup.sh

ENTRYPOINT [ "/home/sdc/startup.sh" ]

