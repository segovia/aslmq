#!/bin/bash
if [ "$#" -ne 4 ]; then
    echo "Illegal number of parameters. Example use: "
    echo "./amazonStartSetup1.sh [threads_per_client] [workload] [db_connections_per_server] [samples]"
    echo "./amazonStartSetup1.sh 10 default 15 10000"
    exit
fi

./bash/amazonDBStart.sh 0
ant -Dbuild.properties.file=amazon.public.properties drop-create-tables
ant -Dbuild.properties.file=amazon.public.properties drop-create-monitor-tables

./bash/amazonStop.sh 4 7
./bash/amazonMiddlewareStart.sh 4 5 $3 true $4
./bash/amazonClientStart.sh 6 7 2 3 $1 $2 true $4
