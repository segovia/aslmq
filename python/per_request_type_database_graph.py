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

# print "step -1"
f=open(sys.argv[1])
next(f) # skip first line

#connections,request_type,samples,response_time,std,ci95,statement_exec_time,std,ci95
#          0,           1,      2,            3,  4,   5,                  6,  7,   8      
base_connections              = [[] for x in range(6)]
base_connection_labels        = [[] for x in range(6)]
base_response_time            = [[] for x in range(6)]
base_response_time_std        = [[] for x in range(6)]
base_response_time_ci95       = [[] for x in range(6)]
base_statment_exe_time        = [[] for x in range(6)]
base_statment_exe_time_std    = [[] for x in range(6)]
base_statment_exe_time_ci95   = [[] for x in range(6)]
for row in csv.reader(f):
    if len(row) == 0: continue;
    request_type = int(row[1])
    if request_type > 6: request_type -= 2
    base_connections           [request_type].append(  int(row[0]))
    base_connection_labels     [request_type].append(      row[0])
    base_response_time         [request_type].append(float(row[3])/msInNs)
    base_response_time_std     [request_type].append(float(row[4])/msInNs)
    base_response_time_ci95    [request_type].append(float(row[5])/msInNs)
    base_statment_exe_time     [request_type].append(float(row[6])/msInNs)
    base_statment_exe_time_std [request_type].append(float(row[7])/msInNs)
    base_statment_exe_time_ci95[request_type].append(float(row[8])/msInNs)

# print "step -1"
f=open(sys.argv[2])
next(f) # skip first line
#connections,request_type,samples,response_time,std,ci95,statement_exec_time,std,ci95
#          0,           1,      2,            3,  4,   5,                  6,  7,   8      
larg_connections              = [[] for x in range(6)]
larg_response_time            = [[] for x in range(6)]
larg_response_time_std        = [[] for x in range(6)]
larg_response_time_ci95       = [[] for x in range(6)]
larg_statment_exe_time        = [[] for x in range(6)]
larg_statment_exe_time_std    = [[] for x in range(6)]
larg_statment_exe_time_ci95   = [[] for x in range(6)]
for row in csv.reader(f):
    if len(row) == 0: continue;
    request_type = int(row[1])
    if request_type > 6: request_type -= 2
    larg_connections           [request_type].append(  int(row[0]))
    larg_response_time         [request_type].append(float(row[3])/msInNs)
    larg_response_time_std     [request_type].append(float(row[4])/msInNs)
    larg_response_time_ci95    [request_type].append(float(row[5])/msInNs)
    larg_statment_exe_time     [request_type].append(float(row[6])/msInNs)
    larg_statment_exe_time_std [request_type].append(float(row[7])/msInNs)
    larg_statment_exe_time_ci95[request_type].append(float(row[8])/msInNs)
    
plt.figure(figsize =(10,10))
 
ind = np.arange(len(base_connections[0]))
width = 0.15
opacity = 0.5
colors = ['blue', 'green', 'red', 'orange', 'cyan', 'magenta', 'yellow', 'black']
msg_types_labels = ['send', 'read peek', 'read pop', 'create queue', 'delete queue', 'find queues']

plt.subplots_adjust(hspace=0.08)

plt.subplot2grid((2,1), (0,0))
plt.title("Database Performance: Request Type X Database Connections")
plt.xticks(ind+3.0*width, base_connections[0])
for i in range(6):
    plt.bar(ind+i*width, base_response_time[i], width, alpha=opacity, color=colors[i], label=msg_types_labels[i])
    plt.errorbar(ind+(i+0.5)*width, base_response_time[i], yerr=base_response_time_std[i], ls='None', color="k", capsize=4)
    plt.errorbar(ind+(i+0.5)*width, base_response_time[i], yerr=base_response_time_ci95[i], ls='None', color="r", capsize=4)
ymax = 160
plt.ylim(0, ymax)
plt.ylabel('response time (ms)')
plt.text(0.1, ymax*0.95, 'baseline database', fontsize=14, verticalalignment='top', bbox=dict(boxstyle='square', facecolor='white'))

plt.subplot2grid((2,1), (1,0))
plt.xticks(ind+3.0*width, base_connections[0])
for i in range(6):
    plt.bar(ind+i*width, larg_response_time[i], width, alpha=opacity, color=colors[i], label=msg_types_labels[i])
    plt.errorbar(ind+(i+0.5)*width, larg_response_time[i], yerr=larg_response_time_std[i], ls='None', color="k", capsize=4, label=('standard deviation' if i + 1 == 6 else ''))
    plt.errorbar(ind+(i+0.5)*width, larg_response_time[i], yerr=larg_response_time_ci95[i], ls='None', color="r", capsize=4, label=('95% confidence interval' if i + 1 == 6 else ''))
ymax = 80
plt.ylim(0, ymax)
plt.text(0.1, ymax*0.95, 'large database', fontsize=14, verticalalignment='top', bbox=dict(boxstyle='square', facecolor='white'))
plt.xlabel('number of connections')
plt.ylabel('response time (ms)')

plt.legend(loc='upper center', bbox_to_anchor=(0.5, -0.13), ncol=4, numpoints=1, prop={'size':10})


plt.savefig('gen/' + os.path.basename(sys.argv[1])[0:-10] + '.graph.png', bbox_inches='tight', dpi = 200)
    
