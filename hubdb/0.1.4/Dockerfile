# Dockerfile for the PDC's HubDB service
#
#
# HubDB for PDC-collected aggregate data.
#
# Example:
# sudo docker pull pdcbc/hubdb
# sudo docker run -d --name hubdb -h hubdb --restart=always \
#   -v ${PATH_MONGO_LIVE}:/data/db/:rw \
#   -v ${PATH_MONGO_DUMP}:/data/dump/:rw \
#   pdcbc/hubdb:latest
#
# Folder paths
# - Mongo live db: -v </path/>:/data/db/:rw
# - Mongo dumps:   -v </path/>:/data/dump/:rw
#
FROM mongo:3.0
MAINTAINER derek.roberts@gmail.com
ENV RELEASE 0.1.4


# Packages
#
RUN apt-get update; \
    apt-get install -y \
    git; \
  apt-get clean; \
  rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*


# Prepare /app/ folder
#
WORKDIR /app/
RUN git clone https://github.com/pdcbc/hubdb.git . -b ${RELEASE}
