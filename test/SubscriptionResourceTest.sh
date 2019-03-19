#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/TestSuite.sh"

set -e

echo -e "\nendpoint: /subscription\n"
echo "Given valid subscription claim; Assert subscription access 200 OK"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" -X POST http://192.168.1.2:8080/subscription -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJ2NnlRaE4xV1BNNzNHanNxSmYybSIsInN1YiI6IjkxYTFiNTU3LWZkZTAtNDE4My1iNjI2LWQwZWM0YTNlNTAxZCIsInR5cCI6InN1YiIsInYiOjF9.-mw5k5re6eSR_rLeyyVtD4ZNc7KrY4W70eusIZ87SAE" -H "content-type: application/json")

set +x;assertTrue 200 "$HTTP_CODE"

echo "Given subscription claim for expired subscription; Assert subscription access 401 Unauthorised"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" -X POST http://192.168.1.2:8080/subscription -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJpOGVkZTE1OFhkdHpVZUFYZVFEbSIsInN1YiI6IjU1RkQyQUMzLTdERTItNEM2Ny1CMEY4LTc5RTdDRkEwMjBDMiIsInR5cCI6InN1YiIsInYiOjF9.j9TRWYwXSp8KPhDXS9P1Cz-L2ldBwlZ8Gb4EssLvHzw" -H "content-type: application/json")

set +x;assertTrue 401 "$HTTP_CODE"

echo -e "\nendpoint: /subscription/{account}\n"
echo "Given account, subscription claim for expired subscription; Assert subscription access 401 Unauthorised"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" -X POST http://192.168.1.2:8080/subscription/14810686-4690-4900-ada5-8b0b7338aa39 -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJpOGVkZTE1OFhkdHpVZUFYZVFEbSIsInN1YiI6IjU1RkQyQUMzLTdERTItNEM2Ny1CMEY4LTc5RTdDRkEwMjBDMiIsInR5cCI6InN1YiIsInYiOjF9.j9TRWYwXSp8KPhDXS9P1Cz-L2ldBwlZ8Gb4EssLvHzw" -H "content-type: application/json")

set +x;assertTrue 401 "$HTTP_CODE"
