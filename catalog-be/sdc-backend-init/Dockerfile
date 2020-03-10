FROM onap/base_sdc-python:1.6.0-SNAPSHOT-latest

COPY --chown=sdc:sdc chef-solo /home/sdc/chef-solo/

COPY --chown=sdc:sdc chef-repo/cookbooks /home/sdc/chef-solo/cookbooks/

COPY --chown=sdc:sdc startup.sh /home/sdc/

RUN chmod 770 /home/sdc/startup.sh

ENTRYPOINT [ "/home/sdc/startup.sh" ]
