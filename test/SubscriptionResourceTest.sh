#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/TestSuite.sh"

set -e

echo "Given expired receipt data; Assert subscription claim 204 NO CONTENT"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" http://192.168.1.2:8080/subscription/transactions -H "content-type: application/json" -d @"$DIR/receipt.json")

set +x;assertTrue 204 "$HTTP_CODE"

echo "Given valid subscription claim; Assert subscription access 200 OK"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" http://192.168.1.2:8080/subscription -d '{"account_identifier":"14810686-4690-4900-ada5-8b0b7338aa39"}' -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJpOGVkZTE1OFhkdHpVZUFYZVFEbSIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJleHAiOjI1NTEzNzExNzcsInR5cCI6InN1YiIsInYiOjF9.A_1sF-ZN8Ti1h8ugY_1Ipl9XKH84q08DuQ-AsCz6uPQ" -H "content-type: application/json")

set +x;assertTrue 200 "$HTTP_CODE"

echo "Given expired subscription claim; Assert subscription access 401 Unauthorised"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" http://192.168.1.2:8080/subscription -d '{"account_identifier":"14810686-4690-4900-ada5-8b0b7338aa39"}' -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJpOGVkZTE1OFhkdHpVZUFYZVFEbSIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJleHAiOjAsInR5cCI6InN1YiIsInYiOjF9.xvZKkzr3tOjn77UX17WDBfuw7nntsu97sXdTEE3SsSY" -H "content-type: application/json")

set +x;assertTrue 401 "$HTTP_CODE"
