#!/bin/bash
echo "Starting a local Wildfly, pointing to a Postgres DB running @ localhost"
mvn clean wildfly:run -Ppostgres -f pom.xml;
