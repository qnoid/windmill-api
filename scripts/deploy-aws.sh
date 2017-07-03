#!/bin/bash
echo "Copying windmill war to AWS, you need to have the your war deployed with AWS properties";
scp -i ~/.ssh/windmill.pem $1 ubuntu@api.windmill.io:~/wildfly/standalone/deployments/ROOT.war
