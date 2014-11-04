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
sInNs =  1000000000.0

# print "step -1"
f=open(sys.argv[1])
next(f) # skip first line

#request_type,response_time,std,ci95,serialization_time,std,ci95,deserialization_time,std,ci95,network_time,std,ci95
#           0,            1,  2,   3,                 4,  5,   6,                   7,  8,   9,          10, 11,  12,      
cl_response_time        = [[] for x in range(6)]
cl_response_time_std    = [[] for x in range(6)]
cl_network_time         = [[] for x in range(6)]
cl_network_time_std     = [[] for x in range(6)]

cl_response_time_ci95      = [[] for x in range(6)]
cl_network_time_ci95      = [[] for x in range(6)]
for row in csv.reader(f):
    request_type = int(row[0])
    if request_type > 6: request_type -= 2
    cl_response_time[request_type]      = float(row[1])/msInNs
    cl_response_time_std[request_type]  = float(row[2])/msInNs
    cl_network_time[request_type]       = float(row[10])/msInNs
    cl_network_time_std[request_type]   = float(row[11])/msInNs   
    cl_response_time_ci95[request_type] = float(row[3])/msInNs
    cl_network_time_ci95[request_type]  = float(row[12])/msInNs

# print "step -1"
f=open(sys.argv[2])
next(f) # skip first line
#request_type,response_time,std,ci95,database_time,std,ci95,acquire_conn_time,std,ci95,serialization_time,std,ci95,deserialization_time,std,ci95,release_conn_time,std,ci95,statement_exec_time,std,ci95
#           0,            1,  2,   3,            4,  5,   6,                7,  8,   9,                10, 11,  12,                  13, 14,  15,               16, 17,  18,                 19, 20,  21
response_time           = [[] for x in range(6)]
response_time_std       = [[] for x in range(6)]
database_time           = [[] for x in range(6)]
database_time_std       = [[] for x in range(6)]
acquire_conn_time       = [[] for x in range(6)]
acquire_conn_time_std   = [[] for x in range(6)]
statment_exe_time       = [[] for x in range(6)]
statment_exe_time_std   = [[] for x in range(6)]
others_time             = [[] for x in range(6)]

response_time_ci95      = [[] for x in range(6)]
database_time_ci95      = [[] for x in range(6)]
acquire_conn_time_ci95  = [[] for x in range(6)]
statment_exe_time_ci95  = [[] for x in range(6)]
    
for row in csv.reader(f):
    request_type = int(row[0])
    if request_type > 6: request_type -= 2
    response_time[request_type]             = float(row[1])/msInNs
    response_time_std[request_type]         = float(row[2])/msInNs
    database_time[request_type]             = float(row[4])/msInNs
    database_time_std[request_type]         = float(row[5])/msInNs
    acquire_conn_time[request_type]         = float(row[7])/msInNs
    acquire_conn_time_std[request_type]     = float(row[8])/msInNs
    statment_exe_time[request_type]         = float(row[19])/msInNs
    statment_exe_time_std[request_type]     = float(row[20])/msInNs
    response_time_ci95[request_type]        = float(row[3])/msInNs
    database_time_ci95[request_type]        = float(row[6])/msInNs
    acquire_conn_time_ci95[request_type]    = float(row[9])/msInNs
    statment_exe_time_ci95[request_type]    = float(row[21])/msInNs
    others_time[request_type]           =  cl_response_time[request_type] - cl_network_time[request_type] + response_time[request_type] - database_time[request_type] - acquire_conn_time[request_type] - statment_exe_time[request_type]
    cl_network_time[request_type] = cl_network_time[request_type] - response_time[request_type];
    cl_network_time_std[request_type] = sqrt(pow(cl_network_time_std[request_type],2) + pow(response_time_std[request_type],2));
    cl_network_time_ci95[request_type] = sqrt(pow(cl_network_time_ci95[request_type],2) + pow(response_time_ci95[request_type],2));
    
    
    worst_ci95 = max(response_time_ci95[request_type]/response_time[request_type],
                     database_time_ci95[request_type]/database_time[request_type],
                     acquire_conn_time_ci95[request_type]/acquire_conn_time[request_type],
                     statment_exe_time_ci95[request_type]/statment_exe_time[request_type],
                     cl_response_time_ci95[request_type]/cl_response_time[request_type])
    
    print("Request type %d - other: %.2f%% worst ci95: %.2f%%" % (request_type, others_time[request_type] * 100.0 / cl_response_time[request_type], worst_ci95 * 100.0))
    
    
    
    
ind = np.arange(6)
width = 0.19
opacity = 0.6

error_config = {'ecolor': '0.5'}

fig, ax = plt.subplots()

clrBars     = ax.bar(ind+0*width, cl_response_time,     width, alpha=opacity, color='b', error_kw=error_config, yerr=cl_response_time_std)
clnBars     = ax.bar(ind+1*width, cl_network_time,      width, alpha=opacity, color='r', error_kw=error_config, yerr=cl_network_time_std)
acqBars     = ax.bar(ind+2*width, acquire_conn_time,    width, alpha=opacity, color='g', error_kw=error_config, yerr=acquire_conn_time_std)
dbBars      = ax.bar(ind+3*width, database_time,        width, alpha=opacity, color='m', error_kw=error_config, yerr=database_time_std    )
stBars      = ax.bar(ind+4*width, statment_exe_time,    width, alpha=opacity, color='k', error_kw=error_config, yerr=statment_exe_time_std)  

ax.errorbar(ind+0.5*width, cl_response_time, yerr=cl_response_time_ci95, ls='None', color="r")
ax.errorbar(ind+1.5*width, cl_network_time,  yerr=cl_network_time_ci95, ls='None', color="r")
ax.errorbar(ind+2.5*width, acquire_conn_time, yerr=acquire_conn_time_ci95, ls='None', color="r")
ax.errorbar(ind+3.5*width, database_time, yerr=database_time_ci95, ls='None', color="r")
ax.errorbar(ind+4.5*width, statment_exe_time, yerr=statment_exe_time_ci95, ls='None', color="r")

plt.ylim(0)
plt.show()  
    
