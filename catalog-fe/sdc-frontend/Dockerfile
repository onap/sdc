FROM jetty:9.4.36-jre11-slim

USER root

# Install Chef
RUN set -ex && \
    apt-get update -y && \
    apt-get install -y \
        jq \
        curl \
        curl \
        vim \
        make \
        gcc \
        ruby \
        ruby-dev \
        libffi-dev \
        libxml2-dev && \
    gem install \
        chef:13.8.5 \
        berkshelf:6.3.1 \
        io-console:0.4.6 \
        etc webrick \
        --no-document && \
    gem cleanup && \
    apt-get update -y && \
    apt-get install -y binutils jq && \
    apt-get clean

# Replace Jetty user ID
ENV USER_JETTY="jetty"
ENV GROUP_JETTY="jetty"
ENV UID_JETTY="352070"
ENV GID_JETTY="35953"

# Remove user:
RUN deluser ${USER_JETTY}
#RUN delgroup ${GROUP_JETTY}
RUN echo "${USER_JETTY}:x:${UID_JETTY}:${GID_JETTY}::/home/${USER_JETTY}:Linux User,,,:/home/jetty:/bin/false" >> /etc/passwd
RUN echo "${GROUP_JETTY}:x:${GID_JETTY}:${USER_JETTY}" >> /etc/group
RUN echo "${USER_JETTY}:!:17501:0:99999:7:::" >> /etc/shadow
RUN mkdir /home/${USER_JETTY} && chown ${USER_JETTY}:${GROUP_JETTY} /home/${USER_JETTY}
RUN chown -R jetty:jetty ${JETTY_BASE}/webapps /var/lib/jetty

USER jetty

RUN mkdir -p ${JETTY_BASE}/config/catalog-fe

COPY --chown=jetty:jetty chef-solo ${JETTY_BASE}/chef-solo/

COPY --chown=jetty:jetty chef-repo/cookbooks ${JETTY_BASE}/chef-solo/cookbooks/

ADD --chown=jetty:jetty onboarding*.war         ${JETTY_BASE}/webapps/

ADD --chown=jetty:jetty catalog-fe-*.war        ${JETTY_BASE}/webapps/

COPY --chown=jetty:jetty startup.sh ${JETTY_BASE}

RUN chmod 770 ${JETTY_BASE}/startup.sh

ENTRYPOINT [ "sh", "-c", "${JETTY_BASE}/startup.sh"]
