#!/bin/bash
if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters"
    echo "./dryadClientDeploy.sh [client_ip_start] [client_ip_end]"
    echo "./dryadClientDeploy.sh 6 7"
    exit
fi

IFS=$'\r\n' GLOBIGNORE='*' :; IPS=($(cat bash/amazon-ips.txt))

echo "Deploying client on ip $1 to ip $2";
for i in $(seq $1 $2); do
	echo -e "\n----- ip $i: ${IPS[$i]}"
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "cat /proc/cpuinfo | grep model\ name -m 1"
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "pkill -f 'java -jar'"
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "rm -rf ~/deployment/"
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "mkdir ~/deployment/"
	scp -i ~/.ssh/aws.pem dist/client.zip ec2-user@${IPS[$i]}:~/deployment/
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "unzip ~/deployment/client.zip -d ~/deployment/"
done
