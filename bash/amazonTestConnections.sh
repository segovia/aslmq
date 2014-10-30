#!/bin/bash
if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters"
    echo "./amazonTestConnections.sh [ip_start] [ip_end]"
    echo "./amazonTestConnections.sh 1 5"
    exit
fi

IFS=$'\r\n' GLOBIGNORE='*' :; IPS=($(cat bash/amazon-ips.txt))

for i in $(seq $1 $2); do
	echo -e "\nTesting connection for ip $i: ${IPS[$i]}";
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "cat /proc/cpuinfo | grep model\ name -m 1"
done
