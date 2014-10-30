import matplotlib.pyplot as plt
import csv
import itertools
import math
import operator
import sys
import datetime
import numpy as np
izip = itertools.izip

msInNs = 1000000.0
percentile = 99

print "step -1"
f=open(sys.argv[1])
next(f) # skip first line

print "step 0"
bins = 1
time_step = 0.5 * 1000000000.0
response_time = []
response_time_sum = [0]
database_time_sum = [0]
acquire_conn_time_sum = [0]
serialization_time_sum = [0]
deserialization_time_sum = [0]
release_conn_time_sum = [0]
statement_exec_time_sum = [0]
msg_count = [0]
group = []
# group_percentile = []
print "step 1"
for row in csv.reader(f):
    elapsed_time = int(row[3])
    response_time.append(int(row[4]))
    group.append(response_time[-1])
    if elapsed_time > bins * time_step:
        bins += 1
        response_time_sum.append(0)
        database_time_sum.append(0)
        acquire_conn_time_sum.append(0)
        serialization_time_sum.append(0)
        deserialization_time_sum.append(0)
        release_conn_time_sum.append(0)
        statement_exec_time_sum.append(0)
        msg_count.append(0)
#         group_percentile.append(np.percentile(np.array(group), 95)/msInNs)
        group = []
    
    response_time_sum[-1] += response_time[-1]
    database_time_sum[-1] += int(row[11])
    acquire_conn_time_sum[-1] += int(row[9])
    serialization_time_sum[-1] += int(row[7])
    deserialization_time_sum[-1] += int(row[8])
    release_conn_time_sum[-1] += int(row[10])
    statement_exec_time_sum[-1] += int(row[12])
    msg_count[-1] += 1
# group_percentile.append(np.percentile(np.array(group), percentile)/msInNs)
    

print "step 2"
a = np.array(response_time)
p = np.percentile(a, percentile)/msInNs
print "The " + str(percentile) + " is " + str(p) + "ms"


print "step 3"
average_response_time =             [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(response_time_sum, msg_count)];
average_database_time =             [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(database_time_sum, msg_count)];
average_acquire_conn_time =         [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(acquire_conn_time_sum, msg_count)];
average_serialization_time_sum =    [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(serialization_time_sum, msg_count)];
average_deserialization_time_sum =  [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(deserialization_time_sum, msg_count)];
average_release_conn_time_sum =     [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(release_conn_time_sum, msg_count)];
average_statement_exec_time_sum =   [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(statement_exec_time_sum, msg_count)];
del average_response_time[0]
del average_database_time[0]
del average_acquire_conn_time[0]
del average_serialization_time_sum[0]
del average_deserialization_time_sum[0]
del average_release_conn_time_sum[0]
del average_statement_exec_time_sum[0]
del msg_count[0]
# del group_percentile[0]

steps = range(1,bins)

print "step 4"
plt.plot(
         steps, average_response_time,              'b',
         steps, average_database_time,              'r',
         steps, average_acquire_conn_time,          'g',
         steps, average_serialization_time_sum,     'y',
        steps, average_deserialization_time_sum,   'm',
#         steps, group_percentile,                   'm',
         steps, average_release_conn_time_sum,      'c',
         steps, average_statement_exec_time_sum,    'k'
         )
plt.ylabel('Average response time (ms/second)')
plt.xlabel('Elapsed seconds')
plt.xlim([1,bins-1])
# plt.ylim(0, p)

print "step 5"
# dt = datetime.datetime.now()
# plt.savefig('../gen/avg_resp_time.' + dt.strftime("%Y%m%d%H%M%S") + '.png', bbox_inches='tight')
plt.show()
print "step 6"