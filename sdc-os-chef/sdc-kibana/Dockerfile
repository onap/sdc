FROM onap/base_sdc-kibana:1.3.0-SNAPSHOT-latest

COPY chef-solo /root/chef-solo/

COPY chef-repo/cookbooks/. /root/chef-solo/cookbooks/

COPY startup.sh /root/


# Install specific system libraries to fix CVE vulnerabilities
RUN echo "deb http://ftp.de.debian.org/debian stretch main" >> /etc/apt/sources.list
RUN echo "deb http://ftp.de.debian.org/debian buster main" >> /etc/apt/sources.list
RUN apt-get update

# bash 4.4-5
#   For CVE-2016-7543
RUN apt-get -y install bash=4.4-5

# curl 7.52.1-5+deb9u4
#   For CVE-2016-7167
RUN apt-get -y install libcurl3=7.52.1-5+deb9u4 curl=7.52.1-5+deb9u4

# krb5 1.16-2
#   For CVE-2017-15088 CVE-2017-11462
RUN apt-get -y install krb5-locales=1.16-2 libgssapi-krb5-2=1.16-2 libkrb5-3=1.16-2 libkrb5support0=1.16-2

# openssl 1.1.0h-2
#   For CVE-2016-6303 CVE-2016-2182 CVE-2016-2177 CVE-2016-2176
RUN apt-get -y install openssl=1.1.0h-2

# perl 5.26.1-5
#   For CVE-2017-12883
RUN apt-get -y install perl-base=5.26.1-5

# systemd 234-4
#   For CVE-2017-1000082
RUN apt-get -y install systemd=238-4

# wget 1.19.4-1
#   For CVE-2017-13090 CVE-2017-13089 CVE-2016-7098
RUN apt-get -y install wget=1.19.4-1


RUN chmod 770 /root/startup.sh

ENTRYPOINT [ "/root/startup.sh" ]
