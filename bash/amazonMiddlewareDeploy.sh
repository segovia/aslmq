#!/bin/bash
if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters"
    echo "./amazonMiddlewareDeploy.sh [server_ip_start] [server_ip_end]"
    echo "./amazonMiddlewareDeploy.sh 4 5"
    exit
fi

IFS=$'\r\n' GLOBIGNORE='*' :; IPS=($(cat bash/amazon-ips.txt))

for i in $(seq $1 $2); do
	echo -e "\n----- ip $i: ${IPS[$i]}"
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "cat /proc/cpuinfo | grep model\ name -m 1"
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "pkill -f 'java -jar'"
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "rm -rf ~/deployment/"
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "mkdir ~/deployment/"
	scp -i ~/.ssh/aws.pem dist/middleware.zip ec2-user@${IPS[$i]}:~/deployment/
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "unzip ~/deployment/middleware.zip -d ~/deployment/"
done
