#!/bin/bash
if [ "$#" -ne 3 ]; then
    echo "Illegal number of parameters"
    echo "./amazonCopyGCLogs.sh [filename_prefix] [start_ip] [end_ip]"
    echo "./amazonCopyGCLogs.sh 01_2_mid_08_conn_norm_db 4 5"
    exit
fi

filename_prefix=$1

IFS=$'\r\n' GLOBIGNORE='*' :; IPS=($(cat bash/amazon-ips.txt))

for i in $(seq $2 $3); do
	echo -e "\n----- copying gc log from ip $i: ${IPS[$i]}"
	scp -i ~/.ssh/aws.pem ec2-user@${IPS[$i]}:/home/ec2-user/deployment/gc_log gen/tmp.txt
	cat gen/tmp.txt | grep Full > gen/$i
	rm gen/tmp.txt;
done

python python/join_gc_logs.py gen/$filename_prefix'_gc_log.txt' $(for i in $(seq $2 $3); do echo -n "gen/$i "; done)

for i in $(seq $2 $3); do rm gen/$i; done