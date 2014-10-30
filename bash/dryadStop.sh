#!/bin/bash
if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters"
    exit
fi

echo "Stopping middleware on dryad0$1 to dryad0$2";
for i in $(seq $1 $2); do
	ssh gsegovia@dryad0$i.ethz.ch "pkill -f 'java -jar'"
done