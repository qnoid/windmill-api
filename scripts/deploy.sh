#!/bin/bash
mvn package
scp -i ~/.ssh/wildfly.pem ./target/windmill.war bitnami@$1:./stack/wildfly/standalone/deployments/windmill.war
