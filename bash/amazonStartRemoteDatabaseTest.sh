#!/bin/bash

if [ "$#" -ne 3 ]; then
    echo "Illegal number of parameters. Example use: "
    echo "./amazonStartRemoteDatabaseTest.sh [db_connections_per_server] [samples] [default|large]"
    echo "./amazonStartRemoteDatabaseTest.sh 15 100000 default"
    exit
fi

./bash/amazonDBStart.sh 0
#ant -Dbuild.properties.file=amazon.public.properties -Dmock.level=$3 drop-create-tables-with-mock-procedures
ant -Dbuild.properties.file=amazon.public.properties drop-create-tables
ant -Dbuild.properties.file=amazon.public.properties drop-create-monitor-tables
ant dist
./bash/amazonDatabaseTestDeploy.sh 4
./bash/amazonDatabaseTestStart.sh 4 amazon.properties $1 $2 $3

