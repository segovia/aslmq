#!/bin/bash
if [ "$#" -ne 1 ]; then
    echo "Illegal number of parameters"
    exit
fi


function sql_to_csv { 
	if [ "$#" -lt 2 ]; then
	    echo "Illegal number of parameters"
	    exit
	fi
	
	table=$1
	iter=$2
	suffix=$3
	#ssh gsegovia@dryad09.ethz.ch "/mnt/local/gsegovia/postgres/bin/psql -U postgres -p 40001 -h /mnt/local/gsegovia/ -c \"SET datestyle = 'epoch'\" aslmq_monitor_db"
	ssh gsegovia@dryad09.ethz.ch "/mnt/local/gsegovia/postgres/bin/psql -U postgres -p 40001 -h /mnt/local/gsegovia/ -c \"Copy(select * from $table) To '/mnt/local/gsegovia/tmp.csv' WITH DELIMITER ',' CSV HEADER\" aslmq_monitor_db"
	scp gsegovia@dryad09.ethz.ch:/mnt/local/gsegovia/tmp.csv gen/$iter'_'$table'_'$suffix.csv
} 


sql_to_csv middle_time $1 dryad
sql_to_csv client_time $1 dryad
sql_to_csv experiment $1 dryad
