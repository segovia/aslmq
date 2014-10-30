#!/bin/bash
if [ "$#" -ne 4 ]; then
    echo "Illegal number of parameters"
    echo "./amazonSQLToCSV.sh [ip] [table] [iteration] [suffix]"
    echo "./amazonSQLToCSV.sh 0 middle_time 01 _local_01"
    exit
fi

IFS=$'\r\n' GLOBIGNORE='*' :; IPS=($(cat bash/amazon-ips.txt))
IP=${IPS[$1]}

echo "Extracting measurements from ip $1: $IP";
	
ip=$IP
table=$2
iter=$3
suffix='amazon'$4

filename_prefix=$iter'_'$table'_'$suffix
filename=$iter'_'$table'_'$suffix'.csv'

ssh -i ~/.ssh/aws.pem ec2-user@$ip "~/postgres/bin/psql -U postgres -p 40001 -h ~/ -c \"Copy(select * from $table order by elapsed_time) To '/home/ec2-user/deployment/$filename' WITH DELIMITER ',' CSV HEADER\" aslmq_monitor_db"
ssh -i ~/.ssh/aws.pem ec2-user@$ip "rm -f /home/ec2-user/deployment/$filename_prefix.zip"
ssh -i ~/.ssh/aws.pem ec2-user@$ip "cd /home/ec2-user/deployment/; zip $filename_prefix $filename"
scp -i ~/.ssh/aws.pem ec2-user@$ip:/home/ec2-user/deployment/$filename_prefix.zip gen/$filename_prefix.zip
unzip gen/$filename_prefix.zip -d gen/
rm -f gen/$filename_prefix.zip


filename=$iter'_experiment_amazon'$4'.csv'
ssh -i ~/.ssh/aws.pem ec2-user@$IP "~/postgres/bin/psql -U postgres -p 40001 -h ~/ -c \"Copy(select * from experiment) To '/home/ec2-user/deployment/$filename' WITH DELIMITER ',' CSV HEADER\" aslmq_monitor_db"
scp -i ~/.ssh/aws.pem ec2-user@$IP:/home/ec2-user/deployment/$filename gen/$filename
