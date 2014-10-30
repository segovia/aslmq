#!/bin/bash
if [ "$#" -ne 5 ]; then
    echo "Illegal number of parameters"
    echo "./dryadMiddlewareStart.sh [server_start] [server_end] [db_connections] [monitor_on] [samples]"
    echo "./dryadMiddlewareStart.sh 7 7 15 true 1000000"
    exit
fi

echo "Starting middleware on dryad0$1 to dryad0$2";
for i in $(seq $1 $2); do
	ssh gsegovia@dryad0$i.ethz.ch "java -jar /mnt/local/gsegovia/middleware.jar /mnt/local/gsegovia/dryad.properties $3 $4 $5 > /mnt/local/gsegovia/log 2>&1 &"
done