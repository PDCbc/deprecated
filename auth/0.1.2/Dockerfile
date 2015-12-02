# Dockerfile for the PDC's Auth service
#
# Base image
#
FROM phusion/passenger-nodejs


# Update system, install DACS
#
ENV DEBIAN_FRONTEND noninteractive
RUN echo 'Dpkg::Options{ "--force-confdef"; "--force-confold" }' \
      >> /etc/apt/apt.conf.d/local
RUN apt-get update; \
    apt-get upgrade -y; \
    apt-get install -y dacs git


# Create startup script and make it executable
#
RUN mkdir -p /etc/service/app/
RUN ( \
      echo "#!/bin/bash"; \
      echo "#"; \
      echo "set -e -o nounset"; \
      echo ""; \
      echo ""; \
      echo "# Prepare DACS"; \
      echo "#"; \
      echo "if [ ! -d \${DACS_STOREDIR}/federations/\${DACS_FEDERATION}/\${DACS_JURISDICTION}/ ]"; \
      echo "then"; \
      echo "  ("; \
      echo "    mkdir -p \${DACS_STOREDIR}/federations/\${DACS_FEDERATION}/\${DACS_JURISDICTION}/"; \
      echo "    cp /app/federations/dacs.conf \${DACS_STOREDIR}/federations/"; \
      echo "    cp /app/federations/site.conf \${DACS_STOREDIR}/federations/"; \
      echo "    touch \${DACS_STOREDIR}/federations/\${DACS_FEDERATION}/roles"; \
      echo "    touch \${DACS_STOREDIR}/federations/\${DACS_FEDERATION}/federation_keyfile"; \
      echo "  )||("; \
      echo "    ERROR: DACS initialization unsuccessful >&2"; \
      echo "  )"; \
      echo "fi"; \
      echo "chown -R app:app \${DACS_STOREDIR}/"; \
      echo "/sbin/setuser app dacskey -uj \${DACS_JURISDICTION} -v \${DACS_STOREDIR}/federations/\${DACS_FEDERATION}/federation_keyfile"; \
      echo ""; \
      echo ""; \
      echo "# Start service"; \
      echo "#"; \
      echo "export CONTROLPORT=\${PORT_AUTH_C}"; \
      echo "export MAINPORT=\${PORT_AUTH_M}"; \
      echo "export DACS=\${DACS_STOREDIR}"; \
      echo "export FEDERATION=\${DACS_FEDERATION}"; \
      echo "export JURISDICTION=\${DACS_JURISDICTION}"; \
      echo "export ROLEFILE=\${DACS_ROLEFILE}"; \
      echo "export KEYFILE=\${DACS_KEYFILE}"; \
      echo "export SECRET=\${NODE_SECRET}"; \
      echo "#"; \
      echo "cd /app/"; \
      echo "/sbin/setuser app npm start"; \
    )  \
      >> /etc/service/app/run
RUN chmod +x /etc/service/app/run


# Prepare /app/ and /etc/dacs/ folders
#
WORKDIR /app/
RUN git clone https://github.com/pdcbc/auth.git -b 0.1.2 .
RUN npm install
RUN mkdir -p /etc/dacs/
RUN chown -R app:app /app/ /etc/dacs/


# Run Command
#
CMD ["/sbin/my_init"]
