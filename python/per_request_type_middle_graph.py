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
from matplotlib.ticker import MultipleLocator


izip = itertools.izip

msInNs = 1000000.0
sInNs =  1000000000.0

# print "step -1"
f=open(sys.argv[1])
next(f) # skip first line

# label,request_type,samples,response_time,std,ci95,database_time,std,ci95,acquire_conn_time,std,ci95,serialization_time,std,ci95,deserialization_time,std,ci95,release_conn_time,std,ci95,statement_exec_time,std,ci95
#     0,           1,      2,            3,  4,   5,            6,  7,   8,                9, 10,  11,                12, 13,  14,                  15, 16,  17,               18, 19,  20,                 21, 22,  23      
labels                   = [[] for x in range(6)]
database_time            = [[] for x in range(6)]
database_time_std        = [[] for x in range(6)]
database_time_ci95       = [[] for x in range(6)]
for row in csv.reader(f):
    if len(row) == 0: continue;
    request_type = int(row[1])
    if request_type > 6: request_type -= 2
    labels                [request_type].append(  row[0].replace('\\n','\n'))
    database_time         [request_type].append(float(row[6])/msInNs)
    database_time_std     [request_type].append(float(row[7])/msInNs)
    database_time_ci95    [request_type].append(float(row[8])/msInNs)

    
plt.figure(figsize =(10,5))
 
ind = np.arange(len(labels[0]))
width = 0.15
opacity = 0.5
colors = ['blue', 'green', 'red', 'orange', 'cyan', 'magenta', 'yellow', 'black']
msg_types_labels = ['send', 'read peek', 'read pop', 'create queue', 'delete queue', 'find queues']

plt.title(sys.argv[2])
plt.xticks(ind+3.0*width, labels[0])
for i in range(6):
    plt.bar(ind+i*width, database_time[i], width, alpha=opacity, color=colors[i], label=msg_types_labels[i])
    plt.errorbar(ind+(i+0.5)*width, database_time[i], yerr=database_time_std[i], ls='None', color="k", capsize=4, label=('standard deviation' if i + 1 == 6 else ''))
    plt.errorbar(ind+(i+0.5)*width, database_time[i], yerr=database_time_ci95[i], ls='None', color="r", capsize=4, label=('95% confidence interval' if i + 1 == 6 else ''))
plt.ylim(0, 46)
plt.ylabel('response time (ms)')

plt.gca().yaxis.set_minor_locator(MultipleLocator(2))
plt.gca().yaxis.grid(True, linestyle='--')
plt.gca().yaxis.grid(b=True, which='minor')

plt.legend(loc='upper center', bbox_to_anchor=(0.5, -0.13), ncol=4, numpoints=1, prop={'size':10})

plt.savefig('gen/' + os.path.basename(sys.argv[1])[0:-18] + '.graph.png', bbox_inches='tight', dpi = 200)
    
