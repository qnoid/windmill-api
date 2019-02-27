#!/bin/bash
echo "Build windmill war with Postgres Datasource"
mvn clean package -Ppostgres -f pom.xml;
