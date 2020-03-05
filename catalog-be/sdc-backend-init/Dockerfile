FROM onap/base_sdc-python:1.7.0

# user/group args are inherited from base_sdc-python base image
COPY --chown=$user:$group scripts /home/$user/scripts

RUN chmod 770 -R /home/$user/scripts && \
    cd /home/$user/scripts && \
    python setup.py install --user

ENV PATH=$PATH:/home/$user/.local/bin

COPY --chown=$user:$group chef-solo /home/$user/chef-solo/

COPY --chown=$user:$group chef-repo/cookbooks /home/$user/chef-solo/cookbooks/

COPY --chown=$user:$group startup.sh /home/$user/

RUN chmod 770 /home/$user/startup.sh

WORKDIR /home/$user/

ENTRYPOINT /home/${user}/startup.sh
