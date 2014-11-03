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
from scipy import interpolate

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

plt.figure(figsize =(10,4))

if isNetwork is False: plt.subplot2grid((18,2), (0,0), rowspan=8, colspan=2)
else: plt.subplot2grid((17,2), (0,0), rowspan=11, colspan=2)

f_interp = interpolate.splrep(response_time_connections, response_time, s=0)
f_interp_x = np.arange(response_time_connections[0], response_time_connections[-1]+1, 1.0)
plt.plot(f_interp_x, interpolate.splev(f_interp_x, f_interp, der=0), '--k', alpha=0.5)

plt.plot(response_time_connections, response_time, 'bo')
plt.title(title)
plt.ylabel('(ms)')
plt.errorbar(response_time_connections, response_time, yerr=response_time_std, ls='None', color="k", capsize=8)
plt.errorbar(response_time_connections, response_time, yerr=response_time_ci95, ls='None', color="r", capsize=8)
plt.xticks(np.arange(0, response_time_connections[-1]+1, 8.0))
plt.ylim(0, 60)

if isNetwork is False: plt.subplot2grid((18,2), (10,0), rowspan=8, colspan=2)
else: plt.subplot2grid((17,2), (0,0), rowspan=5, colspan=2)

f_interp = interpolate.splrep(throughput_connections, throughput, s=0)
f_interp_x = np.arange(throughput_connections[0], throughput_connections[-1]+1, 1.0)
plt.plot(f_interp_x, interpolate.splev(f_interp_x, f_interp, der=0), '--k', alpha=0.5)

plt.plot([],[], 'bo', label='response time mean')
plt.plot([],[], 'go', label='throughput time mean')
plt.plot(throughput_connections, throughput, 'go')
plt.xlabel('number of clients')
plt.ylabel('(messages/s)')
plt.errorbar(throughput_connections, throughput, yerr=throughput_std, ls='None', color="k", capsize=8, label='standard deviation')
plt.errorbar(throughput_connections, throughput, yerr=throughput_ci95, ls='None', color="r", capsize=8, label='95% confidence interval')
plt.plot([], [], '--k', alpha=0.5, label='cubic-spline interpolation')
plt.xticks(np.arange(0, throughput_connections[-1]+1, 8.0))
plt.ylim(0,6000)
plt.legend(loc='upper center', bbox_to_anchor=(0.5, -0.3), ncol=3, numpoints=1, prop={'size':10})



plt.savefig('gen/' + os.path.basename(prefix)[0:-1] + '.perf_x_client_number.png', bbox_inches='tight', dpi = 200)

# plt.show()