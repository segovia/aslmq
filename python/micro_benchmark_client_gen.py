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
percentile = 99
groupPercentile = 95

filename = sys.argv[1]
if filename[-4:] != '.csv': 
    exit(0)
    
filenameStart = int(sys.argv[2])
filenameEnd = int(sys.argv[3])
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
response_time           = []
serialization_time      = []
deserialization_time    = []
network_time            = []

# print "step 1"
elapsed_time = 0
error_client_id = []
error_type = []
error_time = []
#    id,experiment_id,client_id,elapsed_time,response_time,request_type,response_type,serialization_time,deserialization_time,network_time
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
        error_client_id.append(int(row[2]))
        error_time.append(elapsed_time/sInNs)
        error_type.append(request_type)
        continue
#         raise NameError('A message had an error response')
    
    response_time       .append(int(row[ 4]))
    serialization_time  .append(int(row[ 7]))
    deserialization_time.append(int(row[ 8]))
    network_time        .append(int(row[ 9]))


print "label,samples,response_time,,std,ci95,serialization_time,std,ci95,deserialization_time,std,ci95,network_time,std,ci95"

sys.stdout.write(os.path.basename(filename)[filenameStart:filenameEnd] + ',' + str(len(response_time)) + ',')

response_time_array         = np.array(response_time       )
serialization_time_array    = np.array(serialization_time  )
deserialization_time_array  = np.array(deserialization_time)
network_time_array          = np.array(network_time        )

response_ci95           = scipy.stats.sem(response_time_array)          * sp.stats.t.ppf((1+0.95)/2., len(response_time_array)-1)
serialization_ci95      = scipy.stats.sem(serialization_time_array)     * sp.stats.t.ppf((1+0.95)/2., len(serialization_time_array)-1)
deserialization_ci95    = scipy.stats.sem(deserialization_time_array)   * sp.stats.t.ppf((1+0.95)/2., len(deserialization_time_array)-1)
network_ci95            = scipy.stats.sem(network_time_array)           * sp.stats.t.ppf((1+0.95)/2., len(network_time_array)-1)

sys.stdout.write(str(np.mean(response_time_array))          + ',' + str(np.std(response_time_array))          + ',' + str(response_ci95) + ',')
sys.stdout.write(str(np.mean(serialization_time_array))     + ',' + str(np.std(serialization_time_array))     + ',' + str(serialization_ci95) + ',')
sys.stdout.write(str(np.mean(deserialization_time_array))   + ',' + str(np.std(deserialization_time_array))   + ',' + str(deserialization_ci95) + ',')
sys.stdout.write(str(np.mean(network_time_array))           + ',' + str(np.std(network_time_array))           + ',' + str(network_ci95) + '\n')
    
    
print "Error count: " + str(len(error_type))
print str(error_client_id)
print str(error_type)
print str(error_time)

