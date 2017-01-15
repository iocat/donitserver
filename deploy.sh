#!/bin/bash

DB_STORE=$HOME/donitdeploy/cockroach-data
cockroach start --http-port=2000 --port=5432 --store=path=$DB_STORE --background
mvn jetty:run
