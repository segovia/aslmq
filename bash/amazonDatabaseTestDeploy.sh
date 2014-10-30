#!/bin/bash
if [ "$#" -ne 1 ]; then
    echo "Illegal number of parameters"
    echo "./amazonDatabaseTestDeploy.sh [ip]"
    echo "./amazonDatabaseTestDeploy.sh 0"
    exit
fi

IFS=$'\r\n' GLOBIGNORE='*' :; IPS=($(cat bash/amazon-ips.txt))

echo -e "\n----- ip $i: ${IPS[$1]}"
ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$1]} "cat /proc/cpuinfo | grep model\ name -m 1"
ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$1]} "pkill -f 'java -jar'"
ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$1]} "rm -rf ~/deployment/"
ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$1]} "mkdir ~/deployment/"
scp -i ~/.ssh/aws.pem dist/dbTest.zip ec2-user@${IPS[$1]}:~/deployment/
ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$1]} "unzip ~/deployment/dbTest.zip -d ~/deployment/"
