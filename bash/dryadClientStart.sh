#!/bin/bash
if [ "$#" -ne 8 ]; then
    echo "Illegal number of parameters. Example use: "
    echo "./dryadClientStart.sh [client_start] [client_end] [server_start] [server_end] [accounts_per_client] [workload] [monitor_on] [samples]"
    echo "./dryadClientStart.sh 5 6 7 8 10 default true 10000"
    exit
fi

client=($(seq $1 $2))
middleware=($(seq $3 $4))
ids_per_client=$5
min_id=1
max_id=$(($min_id+$ids_per_client-1))
echo "Starting client on dryad0$1 to dryad0$2 connecting to middleware on dryad0$3 to dryad0$4 ";
for ((i=0;i<${#client[@]};++i)); do
	ssh gsegovia@dryad0${client[i]}.ethz.ch "java -jar /mnt/local/gsegovia/client.jar /mnt/local/gsegovia/dryad.properties dryad0${middleware[i]}.ethz.ch $min_id $max_id $6 $7 $8 > /mnt/local/gsegovia/log 2>&1 &"
	min_id=$(($max_id+1))
	max_id=$(($min_id+$ids_per_client-1))
done
