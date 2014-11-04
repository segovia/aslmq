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

seconds_per_step = 1.0
msInNs = 1000000.0
sInNs =  1000000000.0

# print "step -1"
prefix=sys.argv[1]
filename = prefix + 'per_second.csv'
f=open(filename)

mean_response_time = []
std_max_response_time = []
std_min_response_time = []
throughput = []
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
        std_max_response_time.append(  float(row[1]) + mean_response_time[-1])
        std_min_response_time.append(- float(row[1]) + mean_response_time[-1])
        throughput.append(float(row[2]))
        std_max_throughput.append(  float(row[3]) + throughput[-1])
        std_min_throughput.append(- float(row[3]) + throughput[-1])
    rowCount += 1

print seconds_per_step

###### read gc log
gc_middle_events_mat = rmgl.read_middle_gc_log_values(prefix)
gc_middle_events = []
gc_middle_events_y = []
for event_list in gc_middle_events_mat:
    for event in event_list:
        gc_middle_events.append(event-start*seconds_per_step)
        gc_middle_events_y.append(0.1)
        
gc_client_events_mat = rcgl.read_client_gc_log_values(prefix)
gc_client_events = []
gc_client_events_y = []
for event_list in gc_client_events_mat:
    for event in event_list:
        gc_client_events.append(event-start*seconds_per_step)
        gc_client_events_y.append(0.1)

steps = [x*seconds_per_step for x in range(0, rowCount - 5)]

# print "step 4"
plt.figure(figsize =(10,6))
plt.subplot(3,1,1)

plt.title('System Trace')
plt.plot(steps, mean_response_time, 'b')
plt.fill_between(steps, std_max_response_time, std_min_response_time, facecolor='yellow', alpha=0.3)
plt.ylabel('response time (ms)')
# plt.yscale('log')
plt.xlim(steps[0],steps[-1])
ymax = 50
plt.ylim(0,ymax)
plt.text(30, ymax*0.93, 'scaled to mean', fontsize=12, verticalalignment='top', bbox=dict(boxstyle='square', facecolor='white', alpha=0.7))


plt.subplot(3,1,2)
plt.plot(steps, mean_response_time, 'b')
plt.fill_between(steps, std_max_response_time, std_min_response_time, facecolor='yellow', alpha=0.3)
plt.ylabel('response time (ms)')
# plt.yscale('log')
plt.xlim(steps[0],steps[-1])
ymax = 800
plt.ylim(0,ymax)
plt.text(30, ymax*0.93, 'scaled to standard deviation', fontsize=12, verticalalignment='top', bbox=dict(boxstyle='square', facecolor='white', alpha=0.7))

plt.subplot(3,1,3)
plt.plot(steps, throughput, 'b')
plt.plot([],[], color='blue', label='mean')
plt.plot([],[], color='yellow', alpha=0.5, label='standard deviation', linewidth=8)
plt.fill_between(steps, std_max_throughput, std_min_throughput, facecolor='yellow', alpha=0.3)
plt.ylabel('throughput (req/s)')
plt.xlabel('elapsed seconds')
# plt.yscale('log')
plt.xlim(steps[0],steps[-1])
# plt.ylim(0,800)
plt.ylim(0)



plt.legend( loc='upper center', bbox_to_anchor=(0.5, -0.3), numpoints=1, ncol=4, prop={'size':10})


# print "step 5"
# dt = datetime.datetime.now()
# plt.savefig('gen/' + filename[0:-4] + '.avg_resp_time.' + dt.strftime("%Y%m%d%H%M%S") + '.png', bbox_inches='tight')
plt.savefig('gen/' + os.path.basename(prefix)[0:-1] + '.system_trace.png', bbox_inches='tight', dpi = 200)
# plt.show()
# print "step 6"

