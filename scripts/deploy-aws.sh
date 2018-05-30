#!/bin/bash
echo "Copying windmill war to AWS, you need to have the your war deployed with AWS properties";
scp -i ~/.ssh/windmill.pem $1 ubuntu@52.18.247.225:~/tmp/ROOT.war
# do not copy directly to ~/wildfly/standalone/deployments/ROOT.war
