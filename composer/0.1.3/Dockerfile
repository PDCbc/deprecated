# Dockerfile for the PDC's Composer (a.k.a. Hub) service
#
#
# Composer for aggregate data queries
#
# Linked containers
# - Link, HubDB:     --link hubdb:hubdb
#
# External ports
# - AutoSSH port:    -p <hostPort>:22
# - Web UI port:     -p <hostPort>:3002
#
# Folder paths
# - authorized_keys: -v </path/>:/home/autossh/.ssh/:ro
# - known_hosts:     -v </path/>:/root/.ssh/:rw
# - SSH keys:        -v </path/>:/etc/ssh/:rw
# - job params:      -v </path/>:/app/util/scheduled_job_params:rw
#
# Releases
# - https://github.com/PDCbc/hapi/releases
#
#
FROM phusion/passenger-ruby19
MAINTAINER derek.roberts@gmail.com
ENV RELEASE 0.1.3


# Update system, install Lynx
#
RUN apt-get update; \
    apt-get install -y \
      git; \
    apt-get clean; \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*


# Enable and configure SSH (for AutoSSH user/tunnel)
#
RUN rm -f /etc/service/sshd/down; \
    adduser --quiet --disabled-password --home /home/autossh autossh 2>&1


# Prepare /app/ folder
#
WORKDIR /app/
RUN git clone https://github.com/pdcbc/composer.git . -b ${RELEASE}; \
    chown -R app:app /app/; \
    /sbin/setuser app bundle install --path vendor/bundle; \
    sed -i -e "s/localhost:27017/hubdb:27017/" config/mongoid.yml


# Create startup script and make it executable
#
RUN mkdir -p /etc/service/app/; \
    ( \
      echo "#!/bin/bash"; \
      echo "#"; \
      echo "set -e -o nounset"; \
      echo ""; \
      echo ""; \
      echo "# Create Endpoint public keys file (authorized_keys)"; \
      echo "#"; \
      echo "if [ ! -f /home/autossh/.ssh/authorized_keys ]"; \
      echo "then"; \
      echo "  mkdir -p /home/autossh/.ssh/"; \
      echo "  touch /home/autossh/.ssh/authorized_keys"; \
      echo "fi"; \
      echo "chown -R autossh:autossh /home/autossh/.ssh/"; \
      echo ""; \
      echo "# Start service"; \
      echo "#"; \
      echo "cd /app/"; \
      echo "chown -R app:app /app/"; \
      echo "/sbin/setuser app bundle install"; \
      echo "/sbin/setuser app bundle exec script/delayed_job start"; \
      echo "/sbin/setuser app bundle exec rails server -p 3002"; \
      echo "/sbin/setuser app bundle exec script/delayed_job stop"; \
    )  \
      >> /etc/service/app/run; \
    chmod +x /etc/service/app/run


# Run Command
#
CMD ["/sbin/my_init"]
