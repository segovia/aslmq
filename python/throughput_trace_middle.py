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
filename = prefix + 'middle_time.csv'
f=open(filename)
next(f) # skip first line

# print "step 0"
bins = 1
seconds_per_step = 1.0
time_step = seconds_per_step * sInNs

msg_count = [0]
total_msg = 0
# print "step 1"
elapsed_time = 0

error_client_id = []
error_request_type = []
error_type = []
error_time = []
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
        msg_count.append(0)
    
    msg_count[-1] += 1
    
# print "step 2"



# print "step 3"
throughput_per_second           =   [c/seconds_per_step for c in msg_count];


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
throughput_per_second                           = throughput_per_second                         [start:end]
bins = end-start

steps = range(1,bins+1)


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
         steps, throughput_per_second,              'b',
         gc_middle_events,gc_middle_events_y,'ro',
         gc_client_events,gc_client_events_y,'o'
         )
plt.ylabel('Average response time (ms/second)')
plt.xlabel('Elapsed seconds %d' % int(tp_mean) + ' - %.2f%%' % (tp_ci95 * 100.0 / tp_mean))
plt.xlim([1,bins])
plt.ylim(0, 6000)

# print "step 5"
# dt = datetime.datetime.now()
# plt.savefig('gen/' + filename[0:-4] + '.avg_resp_time.' + dt.strftime("%Y%m%d%H%M%S") + '.png', bbox_inches='tight')
plt.savefig('gen/' + os.path.basename(filename)[0:-4] + '.throughput.png', bbox_inches='tight')
plt.show()
# print "step 6"

#find measurements/full_default -maxdepth 1 -name *middle* -type f -exec python python/response_time_trace_middle.py {} \;

print "Error count: " + str(len(error_type))
print str(error_client_id)
print str(error_request_type)
print str(error_type)
print str(error_time)
