#!/usr/bin/env bash

echo "Starts a docker container, exporting the default port, with user windmill/pwd windmill and db windmill
docker run --name windmill -e POSTGRES_PASSWORD=windmill -e POSTGRES_USER=windmill -e POSTGRES_DB=windmill -p 5432:5432 -d postgres;