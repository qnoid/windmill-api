#!/bin/bash
echo "Build windmill war with Postgres Datasource"
mvn clean install -Ppostgres -f pom.xml;
