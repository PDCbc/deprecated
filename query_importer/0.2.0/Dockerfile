# Dockerfile for the PDC's queryImporter service
#
# Base image
#
FROM phusion/passenger-nodejs


# Update system, install Node.js 0.12 and Python 2.7
#
RUN curl -sL https://deb.nodesource.com/setup_0.12 | sudo bash -
ENV DEBIAN_FRONTEND noninteractive
RUN echo 'Dpkg::Options{ "--force-confdef"; "--force-confold" }' \
      >> /etc/apt/apt.conf.d/local
RUN apt-get update; \
    apt-get upgrade -y; \
    apt-get install -y \
        nodejs \
        python2.7


# Prepare /app/ folder
#
WORKDIR /app/
RUN git clone https://github.com/pdcbc/queryImporter.git -b 0.2.0 .
RUN npm config set python /usr/bin/python2.7
RUN npm install
CMD node index.js import --mongo-host=hubdb --mongo-db=query_composer_development --mongo-port=27017
