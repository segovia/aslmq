import matplotlib.pyplot as plt
import csv
import itertools
import math
import operator
import sys
import os
import datetime
import numpy as np
import scipy as sp
import scipy.stats
import read_middle_gc_log_values as rmgl
import read_client_gc_log_values as rcgl
from math import sqrt

izip = itertools.izip

msInNs = 1000000.0
sInNs =  1000000000.0
percentile = 99
groupPercentile = 95

# print "step -1"
prefix=sys.argv[1]


# print "step 0"
bins = 1
seconds_per_step = 1.0
time_step = seconds_per_step * sInNs
response_time = []
response_time_bin = []

response_time_sum = [0]
database_time_sum = [0]
acquire_conn_time_sum = [0]
serialization_time_sum = [0]
deserialization_time_sum = [0]
release_conn_time_sum = [0]
statement_exec_time_sum = [0]

msg_count = [0]
total_msg = 0
# print "step 1"
elapsed_time = 0

error_client_id = []
error_request_type = []
error_type = []
error_time = []

f=open(prefix + 'middle_time.csv')
next(f) # skip first line
for row in csv.reader(f):
    elapsed_time = int(row[3])
    request_type = int(row[5])

    total_msg += 1
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
    
    response_time.append(int(row[4]))
    response_time_bin.append(bins-1)
    response_time_sum[-1] += response_time[-1]
    database_time_sum[-1] += int(row[11])
    acquire_conn_time_sum[-1] += int(row[9])
    serialization_time_sum[-1] += int(row[7])
    deserialization_time_sum[-1] += int(row[8])
    release_conn_time_sum[-1] += int(row[10])
    statement_exec_time_sum[-1] += int(row[12])
    msg_count[-1] += 1

f=open(prefix + 'client_time.csv')
next(f) # skip first line
cur_msg = 0
cur_bin = 0
for row in csv.reader(f):
    elapsed_time = int(row[3])
    request_type = int(row[5])
    if request_type < 0 or request_type > 7:
        raise NameError('Unknown request type')
    if (int(row[6]) != 0):
        error_client_id.append(int(row[2]))
        error_time.append(elapsed_time/sInNs)
        error_request_type.append(request_type)
        error_type.append(int(row[6]))
        continue
#         raise NameError('A message had an error response')

    total_msg += 1
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
    
    response_time.append(int(row[4]))
    response_time_bin.append(bins-1)
    response_time_sum[-1] += response_time[-1]
    database_time_sum[-1] += int(row[11])
    acquire_conn_time_sum[-1] += int(row[9])
    serialization_time_sum[-1] += int(row[7])
    deserialization_time_sum[-1] += int(row[8])
    release_conn_time_sum[-1] += int(row[10])
    statement_exec_time_sum[-1] += int(row[12])
    msg_count[-1] += 1


# print "step 2"



# print "step 3"
average_response_time =             [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(response_time_sum, msg_count)];
average_database_time =             [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(database_time_sum, msg_count)];
average_acquire_conn_time =         [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(acquire_conn_time_sum, msg_count)];
average_serialization_time_sum =    [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(serialization_time_sum, msg_count)];
average_deserialization_time_sum =  [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(deserialization_time_sum, msg_count)];
average_release_conn_time_sum =     [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(release_conn_time_sum, msg_count)];
average_statement_exec_time_sum =   [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(statement_exec_time_sum, msg_count)];
throughput_per_second           =   [c/seconds_per_step for c in msg_count];

# del average_database_time[0]
# del average_statement_exec_time_sum[0]
# del msg_count[0]
# del group_percentile[0]
# 
# steps = range(1,bins)

msgsToDiscard = 0
msgsToDiscardEnd = 0
msgsToDiscard = int(total_msg*0.12)
msgsToDiscardEnd = int(total_msg*0.1)
curCount = 0
curBin = 0
start = 0
end = bins
for c in msg_count:
    if curCount < msgsToDiscard:
        start = curBin + 1
    if curCount + c <= total_msg-msgsToDiscardEnd:
        end = curBin + 1
    curBin += 1
    curCount += c

print "start:" + str(start) + " end:" +  str(end)

# # ignore first and last 10% of time
average_response_time               = average_response_time             [start:end]
average_database_time               = average_database_time             [start:end]
average_acquire_conn_time           = average_acquire_conn_time         [start:end]
average_serialization_time_sum      = average_serialization_time_sum    [start:end]
average_deserialization_time_sum    = average_deserialization_time_sum  [start:end]
average_release_conn_time_sum       = average_release_conn_time_sum     [start:end]
average_statement_exec_time_sum     = average_statement_exec_time_sum   [start:end]
throughput_per_second               = throughput_per_second             [start:end]
bins = end-start

steps = range(1,bins+1)



# print "confidence step"
response_time_array = np.array(response_time[msgsToDiscard:total_msg-msgsToDiscard])
p = np.percentile(response_time_array, percentile)/msInNs
# print "The " + str(percentile) + " is " + str(p) + " ms"

mean = np.mean(response_time_array)
std = np.std(response_time_array)
ci95 = scipy.stats.sem(response_time_array) * sp.stats.t.ppf((1+0.95)/2., len(response_time_array)-1) # sem =  standard error of the mean => std/sqrt(len(response_time_array))
ci99 = scipy.stats.sem(response_time_array) * sp.stats.t.ppf((1+0.99)/2., len(response_time_array)-1) # sem =  standard error of the mean => std/sqrt(len(response_time_array))

print "response time for csv: " + str(len(response_time_array)) + "," + str(mean) + "," + str(std) + "," + str(ci95) + "," + str(ci99)
print("%.2f%%" % (ci95 * 100.0 / mean))

throughput_array = np.array(throughput_per_second)
tp_mean = np.mean(throughput_array)
tp_std = np.std(throughput_array)
tp_ci95 = scipy.stats.sem(throughput_array) * sp.stats.t.ppf((1+0.95)/2., len(throughput_array)-1) # sem =  standard error of the mean => std/sqrt(len(response_time_array))
tp_ci99 = scipy.stats.sem(throughput_array) * sp.stats.t.ppf((1+0.99)/2., len(throughput_array)-1) # sem =  standard error of the mean => std/sqrt(len(response_time_array))
print "throughput for csv: " + str(len(throughput_array)) + "," + str(tp_mean) + "," + str(tp_std) + "," + str(tp_ci95) + "," + str(tp_ci99)
print("%.2f%%" % (tp_ci95 * 100.0 / tp_mean))



###### read gc log
gc_middle_events_mat = rmgl.read_middle_gc_log_values(prefix)
gc_middle_events = []
gc_middle_events_y = []
for event_list in gc_middle_events_mat:
    for event in event_list:
        gc_middle_events.append(event-start)
        gc_middle_events_y.append(0.1)
        
gc_client_events_mat = rcgl.read_client_gc_log_values(prefix)
gc_client_events = []
gc_client_events_y = []
for event_list in gc_client_events_mat:
    for event in event_list:
        gc_client_events.append(event-start)
        gc_client_events_y.append(0.1)
    

# print "step 4"
plt.plot(
         steps, average_response_time,              'b',
         steps, average_database_time,              'r',
         steps, average_acquire_conn_time,          'g',
         steps, average_serialization_time_sum,     'y',
        steps, average_deserialization_time_sum,   'm',
#         steps, group_percentile,                   'm',
         steps, average_release_conn_time_sum,      'c',
         steps, average_statement_exec_time_sum,    'k',
         gc_middle_events,gc_middle_events_y,'ro',
         gc_client_events,gc_client_events_y,'o'
         )
plt.ylabel('Average response time (ms/second)')
plt.xlabel('Elapsed seconds')
plt.xlim([1,bins])
plt.ylim(0, 25)

# print "step 5"
# dt = datetime.datetime.now()
# plt.savefig('gen/' + filename[0:-4] + '.avg_resp_time.' + dt.strftime("%Y%m%d%H%M%S") + '.png', bbox_inches='tight')
plt.savefig('gen/' + os.path.basename(filename)[0:-4] + '.avg_resp_time.png', bbox_inches='tight')
plt.show()
# print "step 6"

#find measurements/full_default -maxdepth 1 -name *middle* -type f -exec python python/response_time_trace_middle.py {} \;

print "Error count: " + str(len(error_type))
print str(error_client_id)
print str(error_request_type)
print str(error_type)
print str(error_time)
