#!/bin/bash
echo "Copying windmill war to AWS, you need to have the your war deployed with AWS properties";
scp -i ~/.ssh/windmill.pem ./target/windmill.war ubuntu@api.windmill.io:~/.linuxbrew/opt/wildfly-as/libexec/standalone/deployments/windmill.war
