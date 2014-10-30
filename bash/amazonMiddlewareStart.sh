#!/bin/bash
if [ "$#" -ne 5 ]; then
    echo "Illegal number of parameters"
    echo "./amazonMiddlewareStart.sh [server_ip_start] [server_ip_end] [db_connections] [monitor_on] [samples]"
    echo "./amazonMiddlewareStart.sh 4 5 15 true 10000"
    exit
fi

IFS=$'\r\n' GLOBIGNORE='*' :; IPS=($(cat bash/amazon-ips.txt))

for i in $(seq $1 $2); do
	echo -e "\n----- starting middleware on ip $i: ${IPS[$i]}"
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[$i]} "java -Xloggc:/home/ec2-user/deployment/gc_log -jar ~/deployment/middleware.jar ~/deployment/amazon.properties $3 $4 $5 > ~/deployment/log 2>&1 &"
done
