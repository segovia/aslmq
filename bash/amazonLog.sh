#!/bin/bash
if [ $# -lt 2 ]; then
    echo "Illegal number of parameters"
    exit
fi

IFS=$'\r\n' GLOBIGNORE='*' :; IPS=($(cat bash/amazon-ips.txt))

for i in $(seq $1 $2); do
	echo -e "\n----- Log on ip $i: ${IPS[$i]}"
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "tail ~/deployment/log "${@:3}""
done
