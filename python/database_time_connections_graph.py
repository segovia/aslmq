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
import os
from math import sqrt

izip = itertools.izip

msInNs = 1000000.0
sInNs =  1000000000.0

# print "step -1"
prefix = sys.argv[1]
title = sys.argv[2]
isNetwork = len(sys.argv) > 3
f=open(prefix + 'response_time.csv')
next(f) # skip first line

#connections,samples,response time/throughput,std,ci95,ci99,errors,ci95%
response_time_connections = []
response_time = []
response_time_std = []
response_time_ci95 = []

for row in csv.reader(f):
    if len(row) == 0: continue;
    response_time_connections.append(int(row[0]))
    response_time.append(float(row[2])/1000000)
    response_time_std.append(float(row[3])/1000000)
    response_time_ci95.append(float(row[4])/1000000)

f=open(prefix + 'throughput.csv')
next(f) # skip first line
throughput_connections = []
throughput = []
throughput_std = []
throughput_ci95 = []
for row in csv.reader(f):
    if len(row) == 0: continue;
    throughput_connections.append(int(row[0]))
    throughput.append(float(row[2]))
    throughput_std.append(float(row[3]))
    throughput_ci95.append(float(row[4]))

max_connection = throughput_connections[-1]

plt.figure(figsize =(10,8))

plt.subplot2grid((17,2), (0,0), rowspan=10, colspan=2)
plt.plot(response_time_connections, response_time, 'bo-')
plt.title(title)
plt.ylabel('Mean response time (ms)')
plt.errorbar(response_time_connections, response_time, yerr=response_time_std, ls='None', color="k", capsize=8)
plt.errorbar(response_time_connections, response_time, yerr=response_time_ci95, ls='None', color="r", capsize=8)
plt.xticks(np.arange(0, response_time_connections[-1]+1, 4.0))
if isNetwork is False: plt.ylim(0, 80)
else: plt.ylim(0, 18)

plt.subplot2grid((17,2), (11,0), rowspan=5, colspan=2)
plt.plot(throughput_connections, throughput, 'go-')
plt.xlabel('number of connections')
plt.ylabel('Mean throughput (msg/s)')
plt.errorbar(throughput_connections, throughput, yerr=throughput_ci95, ls='None', color="r", capsize=8, label='95% confidence interval')
plt.errorbar(throughput_connections, throughput, yerr=throughput_std, ls='None', color="k", capsize=8, label='standard deviation')
plt.xticks(np.arange(0, throughput_connections[-1]+1, 4.0))
if isNetwork is False: plt.ylim(0,10000)
else: plt.ylim(0,22000)
plt.legend(loc='upper center', bbox_to_anchor=(0.5, -0.3), ncol=5, numpoints=1, prop={'size':10})



plt.savefig('gen/' + os.path.basename(prefix)[0:-1] + '.perf_x_connections.png', bbox_inches='tight', dpi = 200)

# plt.show()