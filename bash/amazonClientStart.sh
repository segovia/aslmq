#!/bin/bash
if [ "$#" -ne 8 ]; then
    echo "Illegal number of parameters. Example use: "
    echo "./amazonClientStart.sh [client_ip_start] [client_ip_end] [server_ip_start] [server_ip_end] [accounts_per_client] [workload] [monitor_on] [samples]"
    echo "./amazonClientStart.sh 6 7 2 3 10 default true 10000"
    exit
fi

IFS=$'\r\n' GLOBIGNORE='*' :; IPS=($(cat bash/amazon-ips.txt))

client=($(seq $1 $2))
middleware=($(seq $3 $4))
ids_per_client=$5
min_id=1
max_id=$(($min_id+$ids_per_client-1))
for ((i=0;i<${#client[@]};++i)); do
	echo -e "\n----- starting client on ip ${client[i]}: ${IPS[${client[i]}]}. Connecting it to ip ${middleware[i]}: ${IPS[${middleware[i]}]}."
	ssh -i ~/.ssh/aws.pem ec2-user@${IPS[${client[i]}]} "java -Xloggc:/home/ec2-user/deployment/gc_log -jar ~/deployment/client.jar ~/deployment/amazon.properties ${IPS[${middleware[i]}]} $min_id $max_id $6 $7 $8 > ~/deployment/log 2>&1 &"
	min_id=$(($max_id+1))
	max_id=$(($min_id+$ids_per_client-1))
done
