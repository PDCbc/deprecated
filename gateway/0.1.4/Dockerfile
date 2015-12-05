# Dockerfile for the PDC's Gateway (formerly Endpoint) service
#
#
# Receives e2e exports and responds to queries as part of an Endpoint deployment.
# Links to a database.
#
# Example:
# sudo docker pull pdcbc/gateway
# sudo docker run -d --name=gateway -h gateway --restart=always \
#   --link database:database \
#   -v /encrypted/docker/import/.ssh/:/home/autossh/.ssh/:rw"
#   -e gID=<gatewayID> \
#   pdcbc/dclapi
#
# Linked containers
# - Database:   --link database:database
#
# Folder paths
# - SSH keys:   -v </path/>:/home/autossh/.ssh/:ro
#
# Required variables
# - Gateway ID: -e gID=####
#
# Modify default settings
# - Hub IPv4:   -e gID=#.#.#.#
# -   SSH port: -e PORT_AUTOSSH=####
#
# Releases
# - https://github.com/PDCbc/gateway/releases
#
#
FROM phusion/passenger-ruby19
MAINTAINER derek.roberts@gmail.com
ENV RELEASE 0.1.3


# Dockerfile for the PDC's Gateway service, part of an Endpoint deployment
#
#
# Modify default settings at runtime with environment files and/or variables.
# - Set Gateway ID#: -e gID=####
# - Set Hub IP addr: -e IP_HUB=#.#.#.#
# - Use an env file: --env-file=/path/to/file.env
#
# Samples at https://github.com/pdcbc/endpoint.git


# Base image
#
FROM phusion/passenger-ruby19:0.9.17
MAINTAINER derek.roberts@gmail.com


# Set Gateway release tag (https://github.com/PDCbc/gateway/releases)
#
ENV RELEASE_GATEWAY 0.1.4


# Update system and packages
#
ENV DEBIAN_FRONTEND noninteractive
RUN echo 'Dpkg::Options{ "--force-confdef"; "--force-confold" }' \
      >> /etc/apt/apt.conf.d/local; \
    apt-get update; \
    apt-get install -y \
      autossh \
      git; \
    apt-get clean; \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*


# Prepare /app/ folder
#
WORKDIR /app/
RUN git clone https://github.com/pdcbc/gateway.git -b ${RELEASE_GATEWAY} .; \
    mkdir -p ./tmp/pids ./util/files; \
    gem install multipart-post; \
    sed -i -e "s/localhost:27017/database:27017/" config/mongoid.yml; \
    chown -R app:app /app/; \
    /sbin/setuser app bundle install --path vendor/bundle


# Add AutoSSH User
#
RUN adduser --disabled-password --gecos '' --home /home/autossh autossh; \
    chown -R autossh:autossh /home/autossh


# Script for SSH key setup
#
VOLUME /app/sync/
RUN ( \
      echo "#!/bin/bash"; \
      echo "#"; \
      echo "# Exit on errors or unset variables"; \
      echo ""; \
      echo ""; \
      echo "# Create and cat SSH keys, if necessary"; \
      echo "#"; \
      echo "chown -R autossh:autossh /home/autossh"; \
      echo "[ -f /home/autossh/.ssh/id_rsa.pub ]|| \\"; \
      echo "{"; \
      echo "  /sbin/setuser autossh ssh-keygen -b 4096 -t rsa -N \"\" -C dkey\${gID}-\$(date +%Y-%m-%d-%T) -f /home/autossh/.ssh/id_rsa"; \
      echo "  cat /home/autossh/.ssh/id_rsa.pub"; \
      echo "  echo"; \
      echo "  echo"; \
      echo "  echo 'Copy the public key above, then press Enter to test it'"; \
      echo "  echo"; \
      echo "  read ENTER_TO_CONTINUE"; \
      echo "}"; \
      echo ""; \
      echo ""; \
      echo "# Verify public key is loaded and populate known_hosts"; \
      echo "#"; \
      echo "/sbin/setuser autossh ssh -p \${PORT_AUTOSSH} autossh@\${IP_HUB} -o StrictHostKeyChecking=no 'hostname; exit'"; \
      echo "echo"; \
      echo "echo"; \
      echo "echo 'Success!'"; \
      echo "echo"; \
      echo "echo"; \
    )  \
      >> /app/ssh_config.sh; \
    chmod +x /app/ssh_config.sh


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
      echo "IP_HUB=\${IP_HUB:-10.0.2.2}"; \
      echo "PORT_AUTOSSH=\${PORT_AUTOSSH:-22}"; \
      echo "PORT_START_GATEWAY=\${PORT_START_GATEWAY:-40000}"; \
      echo "PORT_REMOTE=\`expr \${PORT_START_GATEWAY} + \${gID}\`"; \
      echo ""; \
      echo ""; \
      echo "# Wait for SSH to be configured"; \
      echo "#"; \
      echo "chown -R autossh:autossh /home/autossh"; \
      echo "while [ ! -f /home/autossh/.ssh/id_rsa.pub ]"; \
      echo "do"; \
      echo "  echo"; \
      echo "  echo"; \
      echo "  echo Please run /app/ssh_config.sh before continuing"; \
      echo "  echo"; \
      echo "  echo"; \
      echo "  sleep 60"; \
      echo "done"; \
      echo ""; \
      echo ""; \
      echo "# Start tunnels"; \
      echo "#"; \
      echo "exec /sbin/setuser autossh /usr/bin/autossh -M0 -p \${PORT_AUTOSSH} -N -R \\"; \
      echo "  \${PORT_REMOTE}:localhost:3001 autossh@\${IP_HUB} -o ServerAliveInterval=15 \\"; \
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
