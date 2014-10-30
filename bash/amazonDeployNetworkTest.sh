#!/bin/bash
./bash/amazonDBStart.sh 0
ant -Dbuild.properties.file=amazon.public.properties drop-create-tables
ant -Dbuild.properties.file=amazon.public.properties drop-create-monitor-tables
ant dist
./bash/amazonMiddlewareDeploy.sh 4 4
./bash/amazonClientDeploy.sh 6 6

