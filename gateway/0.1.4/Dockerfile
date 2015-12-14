# Dockerfile for the PDC's Gateway (formerly Endpoint) service
#
#
# Receives e2e exports and responds to queries as part of an Endpoint deployment.
# Links to a database.
#
# Example:
# sudo docker pull pdcbc/gateway
# sudo docker run -d --name=gateway --restart=always \
#   --link database:database \
#   -v /encrypted/docker/import/.ssh/:/home/autossh/.ssh/:rw"
#   -e gID=9999 \
#   -e DOCTOR_IDS=11111,99999
#   pdcbc/dclapi
#
# Linked containers
# - Database:     --link database:database
#
# Folder paths
# - SSH keys:     -v </path/>:/home/autossh/.ssh/:ro
#
# Required variables
# - Gateway ID:   -e gID=####
# - Doctor IDs:   -e DOCTOR_IDS=#####,#####,...,#####
#
# Modify default settings
# - Composer IP:  -e IP_COMPOSER=#.#.#.#
# - AutoSSH port: -e PORT_AUTOSSH=####
# - Low GW port:  -e PORT_START_GATEWAY=####
#
# Releases
# - https://github.com/PDCbc/gateway/releases
#
#
FROM phusion/passenger-ruby19
MAINTAINER derek.roberts@gmail.com
ENV RELEASE 0.1.4


# Update system and packages
#
RUN apt-get update; \
    apt-get install -y \
      autossh \
      git; \
    apt-get clean; \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*


# Prepare /app/ folder
#
WORKDIR /app/
RUN git clone https://github.com/pdcbc/gateway.git -b ${RELEASE} .; \
    mkdir -p ./tmp/pids ./util/files; \
    gem install multipart-post; \
    sed -i -e "s/localhost:27017/database:27017/" config/mongoid.yml; \
    chown -R app:app /app/; \
    /sbin/setuser app bundle install --path vendor/bundle


# Add AutoSSH User
#
RUN adduser --disabled-password --gecos '' --home /home/autossh autossh; \
    chown -R autossh:autossh /home/autossh


# Startup script for Gateway tunnel
#
RUN SRV=autossh; \
    mkdir -p /etc/service/${SRV}/; \
    ( \
      echo "#!/bin/bash"; \
      echo ""; \
      echo ""; \
      echo "# Set variables"; \
      echo "#"; \
      echo "gID=\${gID:-0}"; \
      echo "IP_COMPOSER=\${IP_COMPOSER:-142.104.128.120}"; \
      echo "PORT_AUTOSSH=\${PORT_AUTOSSH:-2774}"; \
      echo "PORT_START_GATEWAY=\${PORT_START_GATEWAY:-40000}"; \
      echo "PORT_REMOTE=\`expr \${PORT_START_GATEWAY} + \${gID}\`"; \
      echo ""; \
      echo ""; \
      echo "# Check for SSH keys"; \
      echo "#"; \
      echo "sleep 15"; \
      echo "chown -R autossh:autossh /home/autossh"; \
      echo "if [ ! -s /home/autossh/.ssh/id_rsa.pub ]"; \
      echo "then"; \
      echo "  echo"; \
      echo "  echo No SSH keys in /home/autossh/.ssh/."; \
      echo "  echo"; \
      echo "  sleep 3600"; \
      echo "  exit"; \
      echo "fi"; \
      echo ""; \
      echo ""; \
      echo "# Start tunnels, echo key if unsuccessful"; \
      echo "#"; \
      echo "export AUTOSSH_MAXSTART=1"; \
      echo "exec /sbin/setuser autossh /usr/bin/autossh -M0 -p \${PORT_AUTOSSH} -N -R \\"; \
      echo "  \${PORT_REMOTE}:localhost:3001 \${IP_COMPOSER} -o ServerAliveInterval=15 \\"; \
      echo "  -o ServerAliveCountMax=3 -o Protocol=2 -o ExitOnForwardFailure=yes"; \
    )  \
      >> /etc/service/${SRV}/run; \
    chmod +x /etc/service/${SRV}/run


# Startup script for Gateway's Delayed Job app
#
RUN SRV=delayed_job; \
    mkdir -p /etc/service/${SRV}/; \
    ( \
      echo "#!/bin/bash"; \
      echo ""; \
      echo ""; \
      echo "# Start delayed job"; \
      echo "#"; \
      echo "cd /app/"; \
      echo "/sbin/setuser app bundle exec /app/script/delayed_job stop > /dev/null"; \
      echo "rm /app/tmp/pids/server.pid > /dev/null"; \
      echo "exec /sbin/setuser app bundle exec /app/script/delayed_job run"; \
    )  \
      >> /etc/service/${SRV}/run; \
    chmod +x /etc/service/${SRV}/run


# Startup script for Rails server
#
RUN SRV=rails; \
    mkdir -p /etc/service/${SRV}/; \
    ( \
      echo "#!/bin/bash"; \
      echo ""; \
      echo ""; \
      echo "# Set variables"; \
      echo "#"; \
      echo "DOCTOR_IDS=\${DOCTOR_IDS:-cpsid}"; \
      echo ""; \
      echo ""; \
      echo "# Populate providers.txt with DOCTOR_IDS"; \
      echo "#"; \
      echo "/app/providers.sh add \${DOCTOR_IDS}"; \
      echo ""; \
      echo ""; \
      echo "# Start Rails server"; \
      echo "#"; \
      echo "cd /app/"; \
      echo "exec /sbin/setuser app bundle exec rails server -p 3001"; \
    )  \
      >> /etc/service/${SRV}/run; \
    chmod +x /etc/service/${SRV}/run


# Run initialization command
#
CMD ["/sbin/my_init"]
