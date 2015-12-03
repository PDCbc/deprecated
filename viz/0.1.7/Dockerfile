# Dockerfile for the PDC's Visualizer service
#
#
# Visualizer for aggregate data
#
# Modify default settings at runtime with environment files and/or variables.
# - Visualizer ports:  -e PORT_VIZ=<hostPort>
# - Visualizer ports:  -e PORT_VIZ_HTTP=<hostPort>
# - Auth main port:    -e PORT_AUTH_M=<hostPort>
# - Auth control port: -e PORT_AUTH_C<hostPort>
# - Hub API port:      -e PORT_HAPI=<hostPort>
# - Node secret:       -e NODE_SECRET=<string>
# - DACS path:         -e DACS_STOREDIR=<path>
# - Build mode:        -e MODE=<prod/dev>
# - Use an env file:   --env-file=/path/to/file.env
#
# Releases
# - https://github.com/PDCbc/viz/releases
#
#
FROM phusion/passenger-nodejs
MAINTAINER derek.roberts@gmail.com
ENV RELEASE 0.1.7


# Set Gateway release tag (https://github.com/PDCbc/gateway/releases)
#
ENV RELEASE 0.1.7


# Update system, install Python 2.7
#
ENV DEBIAN_FRONTEND noninteractive
RUN echo 'Dpkg::Options{ "--force-confdef"; "--force-confold" }' \
      >> /etc/apt/apt.conf.d/local
RUN apt-get update; \
    apt-get upgrade -y; \
    apt-get install -y python2.7


# Create startup script and make it executable
#
RUN mkdir -p /etc/service/app/
RUN ( \
      echo "#!/bin/bash"; \
      echo "#"; \
      echo "set -e -o nounset"; \
      echo ""; \
      echo ""; \
      echo "# Environment variables"; \
      echo "#"; \
      echo "export PORT=\${PORT_VIZ:-3008}"; \
      echo "export AUTH_MAIN_URL=https://auth:\${PORT_AUTH_M}"; \
      echo "export AUTH_CONTROL_URL=https://auth:\${PORT_AUTH_C}"; \
      echo "export CALLBACK_URL=https://auth:\${PORT_AUTH_C}/auth/callback"; \
      echo "export HUBAPI_URL=https://hapi:\${PORT_HAPI}"; \
      echo "export SECRET=\${NODE_SECRET}"; \
      echo "export DACS=\${DACS_STOREDIR}"; \
      echo ""; \
      echo ""; \
      echo "# Start service"; \
      echo "#"; \
      echo "cd /app/"; \
      echo "/sbin/setuser app npm start"; \
    )  \
      >> /etc/service/app/run
RUN chmod +x /etc/service/app/run


# Prepare /app/ folder
#
WORKDIR /app/
RUN git clone https://github.com/pdcbc/viz.git -b ${RELEASE} .
RUN npm config set python /usr/bin/python2.7
RUN npm install
RUN chown -R app:app /app/


# Run Command
#
CMD ["/sbin/my_init"]
