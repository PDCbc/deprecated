# Dockerfile for the PDC's Customized OSCAR 12 E2E exporter service
#
#
# Modified OSCAR 12 build. Import SQL, filter and export to aggregate data-gathering endpoint.
# Steps from https://github.com/scoophealth/SCOOP-doc/wiki/OSCAR-Setup-Guide.
#
# Example:
# sudo docker pull pdcbc/oscar_e2e
# sudo docker run -d --name=oscar -h oscar --restart=always \
#   --link gateway:gateway \
#   -v /encrypted/docker/import/:/e2e/:ro \
#   pdcbc/oscar
#
# Linked containers
# - Gateway:    --link gateway:gateway
#
# Folder paths
# - SQL import: -v </path/>:/e2e/:ro
#
#
FROM phusion/baseimage
MAINTAINER derek.roberts@gmail.com


# Update system, add repo and install packages
#
ENV DEBIAN_FRONTEND noninteractive
RUN echo 'deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main' \
      >> /etc/apt/sources.list.d/webupd8team-java-trusty.list; \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys EEA14886; \
    echo oracle-java6-installer shared/accepted-oracle-license-v1-1 \
      select true | /usr/bin/debconf-set-selections
RUN apt-get update; \
    apt-get install --no-install-recommends -y \
      libmysql-java \
      mysql-server \
      oracle-java6-installer \
      tomcat6; \
    apt-get autoclean; \
    apt-get clean; \
    rm -rf \
      /var/lib/apt/lists/* \
      /tmp/* \
      /var/tmp/* \
      /usr/share/doc/ \
      /usr/share/doc-base/ \
      /usr/share/man/


# Configure Tomcat6
#
ENV JAVA_HOME="/usr/lib/jvm/java-6-oracle"
ENV CATALINA_HOME="/usr/share/tomcat6"
ENV CATALINA_BASE="/var/lib/tomcat6"
#
RUN mkdir -p \
      ${CATALINA_HOME}/server/classes/ \
      ${CATALINA_HOME}/shared/classes/


# OSCAR 12 WebARchive (.war) and properties
#
WORKDIR ${CATALINA_BASE}/webapps/
COPY ./war/oscar12.war.* ./
RUN cat oscar12.war.* > oscar12.war; \
    rm oscar12.war.*
COPY src/main/resources/oscar_mcmaster.properties /usr/share/tomcat6/oscar12.properties
RUN sed -i \
      -e "s/^#*db_username *=.*/db_username = root/" \
      -e "s/^#*db_password *=.*/db_password=superInsecure/" \
      -e "s/^#*db_name *=.*/db_name = oscar_12_1\?zeroDateTimeBehavior=round&useOldAliasMetadataBehavior=true&jdbcCompliantTruncation=false/" \
      -e "s/^#*E2E_URL *=.*/E2E_URL = http:\/\/gateway:3001\/records\/create/" \
      -e "s/^#*program *=.*/program = off/" \
      -e "s/^#*ModuleNames *= *E2E/ModuleNames = E2E/" \
      -e "s/^#*CASEMANAGEMENT *=.*/CASEMANAGEMENT = all/" \
    /usr/share/tomcat6/oscar12.properties


# Start MySQL and create database
#
WORKDIR /app/
COPY ./database/mysql/ .
RUN service mysql start; \
    mysqladmin -u root password superInsecure; \
    ./createdatabase_bc.sh root superInsecure oscar_12_1; \
    rm -rf \
      /var/lib/apt/lists/* \
      /tmp/* \
      /var/tmp/* \
      /app/


# Cron and script for SQL/E2E import/export
#
WORKDIR /
RUN SCRIPT=/run_export.sh; \
	  ( \
	    echo "# Run SQL/E2E import/export (boot, daily 3:30 AM PDT = 10:30 AM UTC)"; \
			echo "@reboot ${SCRIPT} > /import.log"; \
	    echo "30 10 * * * ${SCRIPT}"; \
	  ) \
	    | crontab -; \
    ( \
      echo "#!/bin/bash"; \
      echo ""; \
      echo ""; \
      echo "# Set variables"; \
      echo "#"; \
      echo "E2E_DIFF=\${E2E_DIFF:-off}"; \
      echo "E2E_DIFF_DAYS=\${E2E_DIFF_DAYS:-14}"; \
      echo ""; \
      echo ""; \
      echo "# Configure oscar12.properties"; \
      echo "#"; \
      echo 'sed -i \'; \
      echo '  -e "s/^#*E2E_DIFF *=.*/E2E_DIFF = ${E2E_DIFF}/" \'; \
      echo '  -e "s/^#*E2E_DIFF_DAYS *=.*/E2E_DIFF_DAYS = ${E2E_DIFF_DAYS}/" \'; \
      echo "/usr/share/tomcat6/oscar12.properties"; \
      echo ""; \
      echo ""; \
      echo "# Start MySQL and import dumps"; \
      echo "#"; \
      echo "service mysql start"; \
      echo 'find /e2e/ -name "*.sql" | \'; \
      echo "  while read IN"; \
      echo "  do"; \
      echo '    mysql --user=root --password=superInsecure oscar_12_1 < "${IN}"'; \
      echo "  done"; \
      echo ""; \
      echo ""; \
      echo "# Start Tomcat6"; \
      echo "#"; \
      echo "mkdir -p /tmp/tomcat6-tmp/"; \
      echo "/sbin/setuser tomcat6 /usr/lib/jvm/java-6-oracle/bin/java \\"; \
      echo "  -Djava.util.logging.config.file=/var/lib/tomcat6/conf/logging.properties \\"; \
      echo "  -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager \\"; \
      echo "  -Djava.awt.headless=true -Xmx1024m -Xms1024m -XX:MaxPermSize=512m -server \\"; \
      echo "  -Djava.endorsed.dirs=/usr/share/tomcat6/endorsed -classpath /usr/share/tomcat6/bin/bootstrap.jar \\"; \
      echo "  -Dcatalina.base=/var/lib/tomcat6 -Dcatalina.home=/usr/share/tomcat6 \\"; \
      echo "  -Djava.io.tmpdir=/tmp/tomcat6-tmp org.apache.catalina.startup.Bootstrap start"; \
    )  \
    >> ${SCRIPT}; \
    chmod +x ${SCRIPT}


# Volume
#
VOLUME /e2e/
