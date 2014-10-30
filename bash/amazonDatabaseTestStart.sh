#!/bin/bash
if [ "$#" -ne 5 ]; then
    echo "Illegal number of parameters"
    echo "./amazonDatabaseTestStart.sh [ip] [properties_file] [db_connections] [samples] [default|large]"
    echo "./amazonDatabaseTestStart.sh 0 amazon.localhost.properties 15 10000 default"
    exit
fi
IFS=$'\r\n' GLOBIGNORE='*' :; IPS=($(cat bash/amazon-ips.txt))

echo -e "\n----- starting db test on ip $i: ${IPS[$1]}"
ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$1]} "java -jar ~/deployment/dbTest.jar ~/deployment/$2 $3 $4 $5 > ~/deployment/log 2>&1 &"
