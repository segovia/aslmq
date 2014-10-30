#!/bin/bash
if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters"
    echo "./dryadClientDeploy.sh [client_start] [client_end]"
    echo "./dryadClientDeploy.sh 5 6"
    exit
fi

echo "Deploying client on dryad0$1 to dryad0$2";
for i in $(seq $1 $2); do
	ssh gsegovia@dryad0$i.ethz.ch "cat /proc/cpuinfo | grep model\ name -m 1"
	ssh gsegovia@dryad0$i.ethz.ch "pkill -f 'java -jar'"
	ssh gsegovia@dryad0$i.ethz.ch "rm -rf /mnt/local/gsegovia/"
	ssh gsegovia@dryad0$i.ethz.ch "mkdir /mnt/local/gsegovia/"
	scp dist/client.zip gsegovia@dryad0$i.ethz.ch:/mnt/local/gsegovia/
	ssh gsegovia@dryad0$i.ethz.ch "unzip /mnt/local/gsegovia/client.zip -d /mnt/local/gsegovia/"
done
