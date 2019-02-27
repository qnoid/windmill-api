#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/TestSuite.sh"

set -e

echo "Given valid receipt data; Assert subscription claim 200 OK"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" http://192.168.1.2:8080/subscription/transactions -H "content-type: application/json" -d @"$DIR/receipt.json")

echo "Given valid subscription claim; Assert subscription access 200 OK"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" http://192.168.1.2:8080/subscription -d '{"account_identifier":"14810686-4690-4900-ada5-8b0b7338aa39"}' -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJpOGVkZTE1OFhkdHpVZUFYZVFEbSIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJleHAiOjMzMTA4MTg4NTc0LCJ0eXAiOiJzdWIiLCJ2IjoxfQ.8wxhpOddfU1tI4rZCWA4RvxSevDcjj0XAVa0ntaqobI" -H "content-type: application/json")

set +x;assertTrue "$HTTP_CODE" 200
