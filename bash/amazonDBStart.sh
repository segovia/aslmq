#!/bin/bash
if [ "$#" -ne 1 ]; then
    echo "Illegal number of parameters"
    echo "./amazonDBStart.sh [db_ip]"
    echo "./amazonDBStart.sh 0"
    exit
fi

IFS=$'\r\n' GLOBIGNORE='*' :; IPS=($(cat bash/amazon-ips.txt))

echo -e "\n----- starting db ip $1: ${IPS[$1]}"
ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$1]} "pkill -f 'java -jar'"
ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$1]} "pkill -f '/home/ec2-user/postgres/bin/postgres'"
ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$1]} "rm -rf ~/deployment/"
ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$1]} "mkdir ~/deployment/"
ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$1]} "rm -rf ~/db_deployment/"
ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$1]} "mkdir ~/db_deployment/"
ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$1]} "~/postgres/bin/postgres -D ~/postgres/db/ -i -p 40001 -k ~/ > ~/db_deployment/db_log 2>&1 &"