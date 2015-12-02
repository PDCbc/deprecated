# Dockerfile for the PDC's DCLAPI service
#
#
# Drug Class Lookup API
# https://github.com/pdcbc/dclapi.git


# Base image
#
FROM phusion/passenger-nodejs
MAINTAINER derek.roberts@gmail.com


# Set Gateway release tag (https://github.com/PDCbc/gateway/releases)
#
ENV RELEASE 0.1.1


# Update system
#
ENV DEBIAN_FRONTEND noninteractive
RUN echo 'Dpkg::Options{ "--force-confdef"; "--force-confold" }' \
      >> /etc/apt/apt.conf.d/local
RUN apt-get update; \
    apt-get install git


# Create startup script and make it executable
#
RUN mkdir -p /etc/service/app/
RUN ( \
      echo "#!/bin/bash"; \
      echo "#"; \
      echo "set -e -o nounset"; \
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
RUN git clone https://github.com/pdcbc/dclapi.git -b ${RELEASE} .
RUN npm install
RUN chown -R app:app /app/


# Run Command
#
CMD ["/sbin/my_init"]
