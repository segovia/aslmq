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

#label,samples,response_time,std,ci95,serialization_time,std,ci95,deserialization_time,std,ci95,network_time,std,ci95
#    0,      1,            2,  3,   4,                 5,  6,   7,                   8,  9,  10,          11, 12,  13,      
cl_label                = []
cl_response_time        = []
cl_response_time_std    = []
cl_response_time_ci95   = []
cl_network_time         = []
cl_network_time_std     = []
cl_network_time_ci95    = []

for row in csv.reader(f):
    if len(row) == 0: continue;
    cl_label             .append(row[0].replace('\\n','\n'))
    cl_response_time     .append(float(row[2])/msInNs)
    cl_response_time_std .append(float(row[3])/msInNs)
    cl_network_time_ci95 .append(float(row[4])/msInNs)
    cl_network_time      .append(float(row[11])/msInNs)
    cl_network_time_std  .append(float(row[12])/msInNs)   
    cl_response_time_ci95.append(float(row[13])/msInNs)

# print "step -1"
f=open(sys.argv[2])
next(f) # skip first line
# clients,samples,response_time,std,ci95,database_time,std,ci95,acquire_conn_time,std,ci95,serialization_time,std,ci95,deserialization_time,std,ci95,release_conn_time,std,ci95,statement_exec_time,std,ci95
#       0,      1,            2,  3,   4,            5,  6,   7,                8,  9,  10,                11, 12,  13,                  14, 15,  16,               17, 18,  19,                 20, 21,  22
label                   = []
response_time           = []
response_time_std       = []
response_time_ci95      = []
database_time           = []
database_time_std       = []
database_time_ci95      = []
acquire_conn_time       = []
acquire_conn_time_std   = []
acquire_conn_time_ci95  = []
statment_exe_time       = []
statment_exe_time_std   = []
statment_exe_time_ci95  = []
others_time             = []

label_count = 0  
for row in csv.reader(f):
    if len(row) == 0: continue;
    label                 .append(row[0].replace('\\n','\n'))
    response_time         .append(float(row[ 2])/msInNs)
    response_time_std     .append(float(row[ 3])/msInNs)
    response_time_ci95    .append(float(row[ 4])/msInNs)
    database_time         .append(float(row[ 5])/msInNs)
    database_time_std     .append(float(row[ 6])/msInNs)
    database_time_ci95    .append(float(row[ 7])/msInNs)
    acquire_conn_time     .append(float(row[ 8])/msInNs)
    acquire_conn_time_std .append(float(row[ 9])/msInNs)
    acquire_conn_time_ci95.append(float(row[10])/msInNs )
    statment_exe_time     .append(float(row[20])/msInNs)
    statment_exe_time_std .append(float(row[21])/msInNs)
    statment_exe_time_ci95.append(float(row[22])/msInNs)
    others_time           .append( cl_response_time[label_count] - database_time[label_count] - acquire_conn_time[label_count])
#     acquire_conn_time[-1] += database_time[-1] + others_time[-1]
    label_count+=1
    
for i in range(len(response_time)):
    if cl_label[i] != label[i]: raise NameError('Labels are not matching: ' + cl_label[i] + ' - ' + label[i])

if len(sys.argv) < 6 or sys.argv[5] != 'big':
    plt.figure(figsize =(10,2.5))
else:
    plt.figure(figsize =(10,5))
 
width = 0.20
opacity = 0.5
colors = ['blue', 'green', 'red', 'orange', 'cyan', 'magenta', 'yellow', 'black']
system_part_labels = ['total time', 'acquire database connection time', 'database time', 'others']
    
    
ind = np.arange(label_count)
plt.title(sys.argv[3].replace('\\n','\n'))
plt.xticks(ind+2.0*width, label, fontsize=12)

plt.bar(ind+0*width, cl_response_time,  width, alpha=opacity, color=colors[0], label=system_part_labels[0])
plt.bar(ind+1*width, acquire_conn_time, width, alpha=opacity, color=colors[1], label=system_part_labels[1])
plt.bar(ind+2*width, database_time,     width, alpha=opacity, color=colors[2], label=system_part_labels[2])
plt.bar(ind+3*width, others_time,       width, alpha=opacity, color=colors[3], label=system_part_labels[3])
plt.errorbar(ind+(0.5)*width, cl_response_time,  yerr=cl_response_time_std,  ls='None', color="k", capsize=4, label ="standard deviation")
plt.errorbar(ind+(1.5)*width, acquire_conn_time, yerr=acquire_conn_time_std, ls='None', color="k", capsize=4)
plt.errorbar(ind+(2.5)*width, database_time,     yerr=database_time_std,     ls='None', color="k", capsize=4)
plt.errorbar(ind+(0.5)*width, cl_response_time,  yerr=cl_response_time_ci95,  ls='None', color="r", capsize=4, label="95% confidence interval")
plt.errorbar(ind+(1.5)*width, acquire_conn_time, yerr=acquire_conn_time_ci95, ls='None', color="r", capsize=4)
plt.errorbar(ind+(2.5)*width, database_time,     yerr=database_time_ci95,     ls='None', color="r", capsize=4)
    
if len(sys.argv) >= 6 and sys.argv[5] == 'big':
    plt.gca().yaxis.set_minor_locator(MultipleLocator(1))
    plt.gca().yaxis.grid(True, linestyle='--')
    plt.gca().yaxis.grid(b=True, which='minor')

if len(sys.argv) >= 7 and sys.argv[6] == '3_factor':
    plt.xlabel('m/n/type: m     = number of middleware\n                n      = number of database connections\n                type = type of database instance (baseline or large)', fontsize=10, ha='left', x=0.0625, bbox=dict(boxstyle='square', facecolor='white', alpha=1.0))

print "Total time"
print np.array(cl_response_time) + np.array(cl_response_time_ci95)
print np.array(cl_response_time) - np.array(cl_response_time_ci95)

print "Acquire time"
print np.array(acquire_conn_time) + np.array(acquire_conn_time_ci95)
print np.array(acquire_conn_time) - np.array(acquire_conn_time_ci95)

print "Database time"
print np.array(database_time) + np.array(database_time_ci95)
print np.array(database_time) - np.array(database_time_ci95)

plt.ylim(0)
plt.ylabel('response time (ms)')

if len(sys.argv) >= 6 and sys.argv[5] == 'big' and len(sys.argv) < 7:
    plt.legend(loc='upper center', bbox_to_anchor=(0.5, -0.07), ncol=3, numpoints=1, prop={'size':10})
else:
    plt.legend(loc='upper center', bbox_to_anchor=(0.5, -0.20), ncol=3, numpoints=1, prop={'size':10})


if len(sys.argv) < 5 or sys.argv[4] != 'no_x_title':
    plt.xlabel('number of clients')

plt.savefig('gen/' + os.path.basename(sys.argv[1])[0:-18] + '.graph.png', bbox_inches='tight', dpi = 200)
    
