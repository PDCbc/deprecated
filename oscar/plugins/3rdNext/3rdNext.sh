#!/bin/bash
#
# Exit on errors or unitialized variables
#
set -e -o nounset


# Save script directory and change to it
#
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd ${DIR}


# Replace any class files
#
if([ -f ReportDate.class ]||[ -f ThirdNextAppointment.class ])
then
	rm *.class
fi
#
javac ReportDate.java
javac ThirdNextAppointment.java


# Run ThirdNextAppointment.java, classpath to connector
#
mkdir -p reports/
java -cp ".:mysql-connector-java-3.0.11.jar" ThirdNextAppointment


# Move output to data/sync directory
#
mkdir -p ../../../sync/
mv reports/* ../../../sync/
