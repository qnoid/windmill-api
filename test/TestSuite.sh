#!/bin/bash

function assertTrue()
{
if [ ${1} -eq ${2} ]; then
echo "*SUCCESS*"
else
echo "*FAILURE* expected: ${1} actual: ${2}"
exit 1
fi
}

