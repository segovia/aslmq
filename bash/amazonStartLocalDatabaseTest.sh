#!/bin/bash

if [ "$#" -ne 3 ]; then
    echo "Illegal number of parameters. Example use: "
    echo "./amazonStartLocalDatabaseTest.sh [db_connections_per_server] [samples] [default|large]"
    echo "./amazonStartLocalDatabaseTest.sh 15 100000 default"
    exit
fi

./bash/amazonDBStart.sh 0
ant -Dbuild.properties.file=amazon.public.properties drop-create-tables
ant -Dbuild.properties.file=amazon.public.properties drop-create-monitor-tables
ant dist
./bash/amazonDatabaseTestDeploy.sh 0
./bash/amazonDatabaseTestStart.sh 0 amazon.localhost.properties $1 $2 $3

