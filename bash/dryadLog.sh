#!/bin/bash
if [ $# -lt 2 ]; then
    echo "Illegal number of parameters"
    exit
fi

for i in $(seq $1 $2); do
	echo "----- Log on dryad0$i -----";
	ssh gsegovia@dryad0$i.ethz.ch "tail /mnt/local/gsegovia/log "${@:3}""
done
