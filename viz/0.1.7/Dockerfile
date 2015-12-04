# Dockerfile for the PDC's Visualizer service
#
#
# Visualizer for aggregate data
#
# Modify default settings at runtime with environment files and/or variables.
# - Viz https port:  -e PORT_VIZ=<hostPort>
# -      http port:  -e PORT_VIZ_HTTP=<hostPort>
# - Auth main port:  -e PORT_AUTH_M=<hostPort>
# -   control port:  -e PORT_AUTH_C=<hostPort>
# - Hub API port:    -e PORT_HAPI=<hostPort>
# - Node secret:     -e NODE_SECRET=<string>
# - Reject non-CA    -e REJECT_NONCA_CERTS=<0/1>
#     certificates?:
# - Use an env file: --env-file=/path/file.env
#
# Releases
# - https://github.com/PDCbc/viz/releases
#
#
FROM phusion/passenger-nodejs
MAINTAINER derek.roberts@gmail.com
ENV RELEASE 0.1.7


# Update system, install Python 2.7
#
RUN apt-get update; \
    apt-get upgrade -y; \
    apt-get install -y \
      python2.7 \
      git; \
    apt-get clean; \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*


# Create startup script and make it executable
#
RUN mkdir -p /etc/service/app/; \
    ( \
      echo "#!/bin/bash"; \
      echo "#"; \
      echo "set -e -o nounset"; \
      echo ""; \
      echo ""; \
      echo "# Environment variables"; \
      echo "#"; \
      echo "export PORT=\${PORT_VIZ:-3004}"; \
      echo "export PORT_VIZ_HTTP=\${PORT_VIZ:-3008}"; \
      echo "export AUTH_MAIN_URL=https://auth:\${PORT_AUTH_M:-3005}"; \
      echo "export AUTH_CONTROL_URL=https://auth:\${PORT_AUTH_C:-3006}"; \
      echo "export CALLBACK_URL=https://auth:\${PORT_AUTH_C:-3006}/auth/callback"; \
      echo "export HUBAPI_URL=https://hapi:\${PORT_HAPI:-3003}"; \
      echo "#"; \
      echo "export MODE=PROD"; \
      echo "export DACS=/etc/dacs"; \
      echo "export NODE_TLS_REJECT_UNAUTHORIZED=\${REJECT_NONCA_CERTS:-0}"; \
      echo "export SECRET=\${NODE_SECRET:-notVerySecret}"; \
      echo ""; \
      echo ""; \
      echo "# Start service"; \
      echo "#"; \
      echo "cd /app/"; \
      echo "/sbin/setuser app npm start"; \
    )  \
      >> /etc/service/app/run; \
    chmod +x /etc/service/app/run


# Prepare /app/ folder
#
WORKDIR /app/
RUN git clone https://github.com/pdcbc/viz.git -b ${RELEASE} .; \
    npm config set python /usr/bin/python2.7; \
    npm install; \
    chown -R app:app /app/


# Run Command
#
CMD ["/sbin/my_init"]
