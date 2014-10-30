#!/bin/bash
if [ "$#" -ne 1 ]; then
    echo "Illegal number of parameters"
    exit
fi

/cygdrive/c/Program\ Files/PostgreSQL/9.3/bin/psql -U postgres -c "Copy(select * from middle_time) To 'C:\Users\Gustavo\workspace\aslmq\gen\\$1_middle_time.csv' WITH DELIMITER ',' CSV HEADER" aslmq_monitor_db
/cygdrive/c/Program\ Files/PostgreSQL/9.3/bin/psql -U postgres -c "Copy(select * from client_time) To 'C:\Users\Gustavo\workspace\aslmq\gen\\$1_client_time.csv' WITH DELIMITER ',' CSV HEADER" aslmq_monitor_db
/cygdrive/c/Program\ Files/PostgreSQL/9.3/bin/psql -U postgres -c "Copy(select * from experiment) To 'C:\Users\Gustavo\workspace\aslmq\gen\\$1_experiment.csv' WITH DELIMITER ',' CSV HEADER" aslmq_monitor_db