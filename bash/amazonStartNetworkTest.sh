#!/bin/bash
if [ "$#" -ne 3 ]; then
    echo "Illegal number of parameters. Example use: "
    echo "./amazonStartNetworkTest.sh [threads_per_client] [workload] [samples]"
    echo "./amazonStartNetworkTest.sh 10 default 10000"
    exit
fi

./bash/amazonDBStart.sh 0
ant -Dbuild.properties.file=amazon.public.properties drop-create-monitor-tables

./bash/amazonStop.sh 4 4
./bash/amazonStop.sh 6 6
./bash/amazonMiddlewareStart.sh 4 4 0 false true
./bash/amazonClientStart.sh 6 6 2 2 $1 $2 true $3
