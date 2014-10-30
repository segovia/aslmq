#!/bin/bash
if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters"
    echo "./amazonDBStop.sh [start_ip] [end_ip]"
    echo "./amazonDBStop.sh 4 5"
    exit
fi

IFS=$'\r\n' GLOBIGNORE='*' :; IPS=($(cat bash/amazon-ips.txt))

for i in $(seq $1 $2); do
	echo -e "\n----- stopping all processes at ip $i: ${IPS[$i]}"
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "pkill -f '/home/ec2-user/postgres/bin/postgres'"
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "pkill -f 'java'"
done