import matplotlib.pyplot as plt
import matplotlib as mpl
from mpl_toolkits.axes_grid1 import make_axes_locatable
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

seconds_per_step = 1.0
msInNs = 1000000.0
sInNs =  1000000000.0

# print "step -1"
prefix=sys.argv[1]
filename = prefix + 'per_second.csv'
f=open(filename)

mean_response_time = []
std_response_time = []
std_max_response_time = []
std_min_response_time = []
throughput = []
std_throughput = []
std_max_throughput = []
std_min_throughput = []
rowCount = 0
start = -1
for row in csv.reader(f):
    if rowCount == 1: 
        start = int(row[0])
        seconds_per_step = float(row[2])
    if rowCount > 4:
        mean_response_time.append(float(row[0]))
        std_response_time.append(float(row[1]))
        std_max_response_time.append(  std_response_time[-1] + mean_response_time[-1])
        std_min_response_time.append(- std_response_time[-1] + mean_response_time[-1])
        throughput.append(float(row[2]))
        std_throughput.append(float(row[3])) 
        std_max_throughput.append(  std_throughput[-1] + throughput[-1])
        std_min_throughput.append(- std_throughput[-1] + throughput[-1])
    rowCount += 1

print seconds_per_step
steps = [x*seconds_per_step for x in range(0, rowCount - 5)]

###### read gc log
        
gc_client_events_mat = rcgl.read_client_gc_log_values(prefix)
gc_client_events = []
gc_client_events_y = []
gc_client_events_w = []
for event_list in gc_client_events_mat:
    for event in event_list:
        time = event[0]-start*seconds_per_step-1
        gc_client_events.append(time)
        gc_client_events_y.append(50000)
        gc_client_events_w.append(event[1]+2)

gc_middle_events_mat = rmgl.read_middle_gc_log_values(prefix)
gc_middle_events = []
gc_middle_events_y = []
gc_middle_events_w = []
for event_list in gc_middle_events_mat:
    for event in event_list:
        time = event[0]-start*seconds_per_step-1
        gc_middle_events.append(time)
        gc_middle_events_y.append(50000)
        gc_middle_events_w.append(event[1]+2)

# print "step 4"
fig = plt.figure(figsize =(10,6))
plt.subplot2grid((3,1), (0, 0))
plt.title('System Trace with JVM Garbage Collection Events')
# plt.plot(steps, mean_response_time, 'k', linewidth=2.0)
# plt.plot(steps, mean_response_time, '#0099FF')
plt.plot(steps, mean_response_time, 'k')
plt.bar(gc_middle_events, gc_middle_events_y,  gc_middle_events_w, alpha=0.7, color='red',linewidth=0)
plt.bar(gc_client_events, gc_client_events_y,  gc_client_events_w, color='green',linewidth=0)

# plt.fill_between(steps, std_max_response_time, std_min_response_time, facecolor='yellow', alpha=0.3)
plt.ylabel('response time (ms)')
plt.xlim(steps[0],steps[-1])
ymax = 50
plt.ylim(0,ymax)


plt.subplot2grid((3,1), (1, 0))
plt.plot(steps, std_response_time, 'b')
plt.bar(gc_middle_events, gc_middle_events_y,  gc_middle_events_w, alpha=0.7, color='red',linewidth=0)
plt.bar(gc_client_events, gc_client_events_y,  gc_client_events_w, color='green',linewidth=0)
plt.ylabel('standard deviation of\nresponse time (ms)')
plt.xlim(steps[0],steps[-1])
ymax = 800
plt.ylim(0,ymax)


plt.subplot2grid((3,1), (2, 0))
plt.plot(steps, throughput, 'k', label = "mean")
plt.plot([],[], 'b', label = "standard deviation")
plt.bar(gc_middle_events, gc_middle_events_y,  gc_middle_events_w, alpha=0.7, color='red',linewidth=0, label="JVM garbage collection event duration on a middleware instance")
plt.bar(gc_client_events, gc_client_events_y,  gc_client_events_w, color='green',linewidth=0, label="JVM garbage collection event duration on client a instance")
plt.ylabel('throughput (req/s)')
plt.xlim(steps[0],steps[-1])
ymax = 6000
plt.ylim(0,ymax)


plt.xlabel('elapsed seconds')
plt.legend( loc='upper center', bbox_to_anchor=(0.5, -0.3), numpoints=1, ncol=2, prop={'size':10})


# print "step 5"
# dt = datetime.datetime.now()
# plt.savefig('gen/' + filename[0:-4] + '.avg_resp_time.' + dt.strftime("%Y%m%d%H%M%S") + '.png', bbox_inches='tight')
plt.savefig('gen/' + os.path.basename(prefix)[0:-1] + '.system_trace_gc.png', bbox_inches='tight', dpi = 200)
# plt.show()
# print "step 6"

