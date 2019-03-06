#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/TestSuite.sh"

set -e

echo -e "\nendpoint: /subscription/transactions\n"
echo "Given expired receipt data; Assert subscription claim 204 NO CONTENT"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" http://192.168.1.2:8080/subscription/transactions -H "content-type: application/json" -d @"$DIR/receipt.json")

set +x;assertTrue 204 "$HTTP_CODE"

echo -e "\nendpoint: /subscription\n"
echo "Given valid subscription claim; Assert subscription access 200 OK"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" -X POST http://192.168.1.2:8080/subscription -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJpOGVkZTE1OFhkdHpVZUFYZVFEbSIsInN1YiI6IjNGNjYwNUQxLTEyNkYtNDBDMC05NkY3LTQ0NzIyNkYwOTVFRSIsInR5cCI6InN1YiIsInYiOjF9.kBIj3WrzpiCX2G3ZMuv9HghJKHhUsdY6hSrFpr4_4Wg" -H "content-type: application/json")

set +x;assertTrue 200 "$HTTP_CODE"

echo "Given subscription claim for expired subscription; Assert subscription access 401 Unauthorised"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" -X POST http://192.168.1.2:8080/subscription -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJpOGVkZTE1OFhkdHpVZUFYZVFEbSIsInN1YiI6IjU1RkQyQUMzLTdERTItNEM2Ny1CMEY4LTc5RTdDRkEwMjBDMiIsInR5cCI6InN1YiIsInYiOjF9.j9TRWYwXSp8KPhDXS9P1Cz-L2ldBwlZ8Gb4EssLvHzw" -H "content-type: application/json")

set +x;assertTrue 401 "$HTTP_CODE"

echo -e "\nendpoint: /subscription/{account}\n"
echo "Given account, subscription claim for expired subscription; Assert subscription access 401 Unauthorised"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" -X POST http://192.168.1.2:8080/subscription/14810686-4690-4900-ada5-8b0b7338aa39 -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJpOGVkZTE1OFhkdHpVZUFYZVFEbSIsInN1YiI6IjU1ZmQyYWMzLTdkZTItNGM2Ny1iMGY4LTc5ZTdjZmEwMjBjMiIsInR5cCI6InN1YiIsInYiOjF9.XoXNUnKJjJ5bXJsMvQQN72lO08fL7EpFQ_8m97vWQkw" -H "content-type: application/json")

set +x;assertTrue 401 "$HTTP_CODE"
