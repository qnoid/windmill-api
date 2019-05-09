#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/TestSuite.sh"

set -e

echo -e "\nendpoint: /export/{authentication}\n"
echo "Given authentication; Assert export 200 OK"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" -L http://192.168.1.2:8080/export/manifest/eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJmZWZkOWY4NC1hOGQxLTQ4NDItODU1Yy1jNDQ2ZjU5NzZlMTYiLCJ0eXAiOiJleHAiLCJ2IjoxfQ.WT8Kx8r9MuJDzAEj_Xz4OSgqpVAIyh1oVS9Ry3P3jCE)

set +x;assertTrue 200 "${HTTP_CODE}"

echo "Given authentication for missing export; Assert export 410 GONE"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" http://192.168.1.2:8080/export/manifest/eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NDE2YzU4ZC0zMWQ5LTQ1NTEtODcwMi00MGQ1ZjczNDEzNDAiLCJ0eXAiOiJleHAiLCJ2IjoxfQ.P5HFc4uorEgkpYnKTxMKahzc6DZ-6DWhbEaYFNqc_Bg)

set +x;assertTrue 410 "${HTTP_CODE}"
