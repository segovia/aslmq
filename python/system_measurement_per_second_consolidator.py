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
from math import sqrt

izip = itertools.izip

msInNs = 1000000.0
sInNs =  1000000000.0
percentile = 99
groupPercentile = 95

# print "step -1"
prefix=sys.argv[1]
filename = prefix + 'client_time.csv'
f=open(filename)
next(f) # skip first line

# print "step 0"
bins = 1
seconds_per_step = 1.0
time_step = seconds_per_step * sInNs
response_time = []
response_time_bin = []

response_time_sum = [0]

response_time_groups = [[]]
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
        response_time_sum.append(0)
        msg_count.append(0)
        response_time_groups.append([])
    
    response_time.append(int(row[4]))
    response_time_bin.append(bins-1)
    response_time_sum[-1] += response_time[-1]
    msg_count[-1] += 1
    response_time_groups[-1].append(response_time[-1])
    


average_response_time = [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(response_time_sum, msg_count)];
throughput_per_second = [c/seconds_per_step for c in msg_count];
std_response_time     = [np.std(np.array(rt_group))/msInNs if len(rt_group) > 0 else 0.0 for rt_group in response_time_groups ]


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
throughput_per_second               = throughput_per_second             [start:end]
std_response_time                   = std_response_time                 [start:end]




response_time_array = np.array(response_time[msgsToDiscard:total_msg-msgsToDiscard])
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

print "response time,response time std,throughput"
for r, std, t in izip(average_response_time, std_response_time, throughput_per_second):
    print str(r) + "," + str(std) + "," + str(int(t))


