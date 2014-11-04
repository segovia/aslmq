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
percentile = 99
groupPercentile = 95

filename = sys.argv[1]
total_msg = sum(1 for line in open(filename)) -1
msgsToDiscard = int(total_msg*0.12)
msgsToDiscardEnd = int(total_msg*0.05)



# print "step -1"
f=open(filename)
next(f) # skip first line

# print "step 0"
bins = 1
seconds_per_step = 1.0
time_step = seconds_per_step * sInNs
elapsed_time            = []
response_time           = [[] for x in range(8)]
statement_exec_time     = [[] for x in range(8)]

# print "step 1"
elapsed_time = 0
error_type = []
error_time = []
line_count = -1
for row in csv.reader(f):
    line_count += 1
    if line_count < msgsToDiscard: continue
    if line_count >= total_msg - msgsToDiscardEnd: break
    elapsed_time = int(row[3])
    request_type = int(row[5])
    if request_type < 0 or request_type > 7:
        raise NameError('Unknown request type')
    if (int(row[6]) != 0):
        error_time.append(elapsed_time/sInNs)
        error_type.append(request_type)
        continue
#         raise NameError('A message had an error response')
#     id,experiment_id,client_id,elapsed_time,database_network_time,request_type,response_type,statement_exec_time
    response_time       [request_type].append(int(row[ 4]))
    statement_exec_time [request_type].append(int(row[7]))
    


print "connections,request_type,samples,response_time,std,ci95,statement_exec_time,std,ci95"
for request_type in range(8):
    if request_type == 5 or request_type == 6: continue # skip login and logout messages

    sys.stdout.write(os.path.basename(filename)[-6:-4] + "," + str(request_type) + ',' + str(len(response_time[request_type])) + ',' )

    response_time_array         = np.array(response_time          [request_type])
    statement_exec_time_array   = np.array(statement_exec_time    [request_type])
    
    response_ci95           = scipy.stats.sem(response_time_array)          * sp.stats.t.ppf((1+0.95)/2., len(response_time_array)-1)
    statement_exec_ci95     = scipy.stats.sem(statement_exec_time_array)    * sp.stats.t.ppf((1+0.95)/2., len(statement_exec_time_array)-1)
    
    sys.stdout.write(str(np.mean(response_time_array))          + ',' + str(np.std(response_time_array))          + ',' + str(response_ci95) + ',')
    sys.stdout.write(str(np.mean(statement_exec_time_array))    + ',' + str(np.std(statement_exec_time_array))    + ',' + str(statement_exec_ci95) + '\n')
    
    
# print "Error count: " + str(len(error_type))
# print str(error_type)
# print str(error_time)

