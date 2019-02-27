#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/TestSuite.sh"

set -e

echo "Given valid subscription access; Assert publish 303 See Other"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" -F "ipa=@$DIR/windmill.ipa" -F "plist=@$DIR/manifest.plist" http://192.168.1.2:8080/account/14810686-4690-4900-ada5-8b0b7338aa39/export -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJjMlZqY21WMCIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJ0eXAiOiJhdCIsInYiOjF9.alkiKMfFS1xtjaDckR5kcuqck7-T1Ax_pm66uA1rYhM")

set +x;assertTrue 303 "${HTTP_CODE}"

echo "Given invalid subscription access; Assert 401 Unauthorized"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" -F "ipa=@$DIR/windmill.ipa" -F "plist=@$DIR/manifest.plist" http://192.168.1.2:8080/account/14810686-4690-4900-ada5-8b0b7338aa39/export -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJjMlZqY21WMCIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJ0eXAiOiJhdCIsInYiOjF9.dB_p1IVx6S7VNPHdryJLbnSuW1BhuGslNMwMZDFj09c")

set +x;assertTrue 401 "${HTTP_CODE}"

echo "Given valid subscription access for a different account; Assert 401 Unauthorized"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" -F "ipa=@$DIR/windmill.ipa" -F "plist=@$DIR/manifest.plist" http://192.168.1.2:8080/account/24810686-4690-4900-ada5-8b0b7338aa40/export -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJjMlZqY21WMCIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJ0eXAiOiJhdCIsInYiOjF9.dB_p1IVx6S7VNPHdryJLbnSuW1BhuGslNMwMZDFj09c")

set +x;assertTrue 401 "${HTTP_CODE}"

echo "Given valid subscription access; Assert GET exports 200 OK"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" http://192.168.1.2:8080/account/14810686-4690-4900-ada5-8b0b7338aa39/exports -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJjMlZqY21WMCIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJ0eXAiOiJhdCIsInYiOjF9.alkiKMfFS1xtjaDckR5kcuqck7-T1Ax_pm66uA1rYhM")

set +x;assertTrue 200 "${HTTP_CODE}"

echo "Given invalid subscription access; Assert 401 Unauthorized"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" http://192.168.1.2:8080/account/14810686-4690-4900-ada5-8b0b7338aa39/exports -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJjMlZqY21WMCIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJ0eXAiOiJhdCIsInYiOjF9.dB_p1IVx6S7VNPHdryJLbnSuW1BhuGslNMwMZDFj09c")

set +x;assertTrue 401 "${HTTP_CODE}"

echo "Given valid subscription access for a different account; Assert 401 Unauthorized"
HTTP_CODE=$(set -x;curl -s -o /dev/null -w "%{http_code}" http://192.168.1.2:8080/account/24810686-4690-4900-ada5-8b0b7338aa40/exports -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJjMlZqY21WMCIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJ0eXAiOiJhdCIsInYiOjF9.alkiKMfFS1xtjaDckR5kcuqck7-T1Ax_pm66uA1rYhM")

set +x;assertTrue 401 "${HTTP_CODE}"
