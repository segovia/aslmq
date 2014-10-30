import matplotlib.pyplot as plt
import csv
import itertools
import math
import operator
import sys
import datetime
import numpy as np
import scipy as sp
import scipy.stats
from math import sqrt

izip = itertools.izip

msInNs = 1000000.0
percentile = 99
groupPercentile = 95

# print "step -1"
f=open(sys.argv[1])
next(f) # skip first line

# print "step 0"
bins = 1
seconds_per_step = 1.0
time_step = seconds_per_step * 1000000000.0
response_time = []
response_time_bin = []
database_time_sum = [0]
statement_exec_time_sum = [0]
msg_count = [0]
group = []
group_percentile = []
# print "step 1"
elapsed_time = 0
for row in csv.reader(f):
    elapsed_time = int(row[3])
    if elapsed_time > bins * time_step:
        bins += 1
        database_time_sum.append(0)
        statement_exec_time_sum.append(0)
        msg_count.append(0)
        grp_array = np.array(group)
#         group_percentile.append((np.mean(grp_array) + scipy.stats.sem(grp_array) * sp.stats.t.ppf((1+0.95)/2., len(grp_array)-1))/msInNs)
#         group_percentile.append(np.percentile(np.array(group), groupPercentile)/msInNs)
        group = []
    
    response_time.append(int(row[4]))
    group.append(response_time[-1])
    response_time_bin.append(bins-1)
    database_time_sum[-1] += response_time[-1]
    statement_exec_time_sum[-1] += int(row[7])
    msg_count[-1] += 1
group_percentile.append(np.percentile(np.array(group), percentile)/msInNs)
    


# print "step 2"



# print "step 3"
average_database_time =             [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(database_time_sum, msg_count)];
average_statement_exec_time_sum =   [0.0 if c == 0 else x/(c*msInNs) for x, c in izip(statement_exec_time_sum, msg_count)];
throughput_per_second           =   [c/seconds_per_step for c in msg_count];

# del average_database_time[0]
# del average_statement_exec_time_sum[0]
# del msg_count[0]
# del group_percentile[0]
# 
# steps = range(1,bins)

# # ignore first and last 10% of time
binsToDiscard = int(0.1 * bins)
average_database_time = average_database_time[binsToDiscard:bins-binsToDiscard-1]
average_statement_exec_time_sum = average_statement_exec_time_sum[binsToDiscard:bins-binsToDiscard-1]
msg_count = msg_count[binsToDiscard:bins-binsToDiscard-1]
group_percentile = group_percentile[binsToDiscard:bins-binsToDiscard-1]
bins = bins - 2*binsToDiscard -1

steps = range(1,bins+1)


i = 0
start = -1
end = -1
for x in response_time_bin:
    if x == binsToDiscard and start == -1:
        start = i
    if x == bins - binsToDiscard and end == -1:
        end = i
    i += 1
    
# print "confidence step"
response_time_array = np.array(response_time[start:end])
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

# print "step 4"
plt.plot(
         steps, average_database_time,              'r',
#         steps, group_percentile,                   'm',
         steps, average_statement_exec_time_sum,    'k'
         )
plt.ylabel('Average response time (ms/second)')
plt.xlabel('Elapsed seconds')
plt.xlim([1,bins])
# plt.ylim(0, p)

# print "step 5"
# dt = datetime.datetime.now()
# plt.savefig('../gen/avg_resp_time.' + dt.strftime("%Y%m%d%H%M%S") + '.png', bbox_inches='tight')
plt.show()
# print "step 6"