#!/bin/bash
if [ "$#" -ne 6 ]; then
    echo "Illegal number of parameters"
    echo "./amazonSQLToCSV.sh [ip] [prefix] [middle_ip_start] [middle_ip_end] [client_ip_start] [client_ip_end]"
    echo "./amazonSQLToCSV.sh 0 01_2_mid_08_conn_norm_db 4 5 6 7"
    exit
fi

prefix=$2

./bash/amazonCopyGCLogs.sh $prefix'_middle_time' $3 $4
./bash/amazonCopyGCLogs.sh $prefix'_client_time' $5 $6

IFS=$'\r\n' GLOBIGNORE='*' :; IPS=($(cat bash/amazon-ips.txt))
IP=${IPS[$1]}

echo "Extracting measurements from ip $1: $IP";
	
ip=$IP

filename_prefix=$prefix'_middle_time'
filename=$prefix'_middle_time.csv'

scp -i ~/.ssh/aws.pem ec2-user@$ip:/home/ec2-user/db_deployment/db_log gen/$prefix'_db_log.txt'

ssh -i ~/.ssh/aws.pem ec2-user@$ip "~/postgres/bin/psql -U postgres -p 40001 -h ~/ -c \"Copy(select * from select_synced_middle_time()) To '/home/ec2-user/deployment/$filename' WITH DELIMITER ',' CSV HEADER\" aslmq_monitor_db"
ssh -i ~/.ssh/aws.pem ec2-user@$ip "rm -f /home/ec2-user/deployment/$filename_prefix.zip"
ssh -i ~/.ssh/aws.pem ec2-user@$ip "cd /home/ec2-user/deployment/; zip $filename_prefix $filename"
scp -i ~/.ssh/aws.pem ec2-user@$ip:/home/ec2-user/deployment/$filename_prefix.zip gen/$filename_prefix.zip
unzip gen/$filename_prefix.zip -d gen/
rm -f gen/$filename_prefix.zip


filename_prefix=$prefix'_client_time'
filename=$prefix'_client_time.csv'
ssh -i ~/.ssh/aws.pem ec2-user@$ip "~/postgres/bin/psql -U postgres -p 40001 -h ~/ -c \"Copy(select * from select_synced_client_time()) To '/home/ec2-user/deployment/$filename' WITH DELIMITER ',' CSV HEADER\" aslmq_monitor_db"
ssh -i ~/.ssh/aws.pem ec2-user@$ip "rm -f /home/ec2-user/deployment/$filename_prefix.zip"
ssh -i ~/.ssh/aws.pem ec2-user@$ip "cd /home/ec2-user/deployment/; zip $filename_prefix $filename"
scp -i ~/.ssh/aws.pem ec2-user@$ip:/home/ec2-user/deployment/$filename_prefix.zip gen/$filename_prefix.zip
unzip gen/$filename_prefix.zip -d gen/
rm -f gen/$filename_prefix.zip


filename=$prefix'_experiment.csv'
ssh -i ~/.ssh/aws.pem ec2-user@$IP "~/postgres/bin/psql -U postgres -p 40001 -h ~/ -c \"Copy(select * from experiment order by id) To '/home/ec2-user/deployment/$filename' WITH DELIMITER ',' CSV HEADER\" aslmq_monitor_db"
scp -i ~/.ssh/aws.pem ec2-user@$IP:/home/ec2-user/deployment/$filename gen/$filename
