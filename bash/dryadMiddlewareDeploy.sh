#!/bin/bash
if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters"
    echo "./dryadMiddlewareDeploy.sh [server_start] [server_end]"
    echo "./dryadMiddlewareDeploy.sh 7 8"
    exit
fi

echo "Deploying middleware on dryad0$1 to dryad0$2";
for i in $(seq $1 $2); do
	ssh gsegovia@dryad0$i.ethz.ch "cat /proc/cpuinfo | grep model\ name -m 1"
	ssh gsegovia@dryad0$i.ethz.ch "pkill -f 'java -jar'"
	ssh gsegovia@dryad0$i.ethz.ch "rm -rf /mnt/local/gsegovia/"
	ssh gsegovia@dryad0$i.ethz.ch "mkdir /mnt/local/gsegovia/"
	scp dist/middleware.zip gsegovia@dryad0$i.ethz.ch:/mnt/local/gsegovia/
	ssh gsegovia@dryad0$i.ethz.ch "unzip /mnt/local/gsegovia/middleware.zip -d /mnt/local/gsegovia/"
done
