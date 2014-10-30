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
percentile = 99
groupPercentile = 95

# print "step -1"
f=open(sys.argv[1])
next(f) # skip first line

# print "step 0"
bins = 1
seconds_per_step = 1.0
time_step = seconds_per_step * sInNs
elapsed_time            = []
response_time           = [[] for x in range(8)]
serialization_time      = [[] for x in range(8)]
deserialization_time    = [[] for x in range(8)]
network_time            = [[] for x in range(8)]

# print "step 1"
elapsed_time = 0
error_client_id = []
error_type = []
error_time = []
#    id,experiment_id,client_id,elapsed_time,response_time,request_type,response_type,serialization_time,deserialization_time,network_time

for row in csv.reader(f):
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
    
    response_time       [request_type].append(int(row[ 4]))
    serialization_time  [request_type].append(int(row[ 7]))
    deserialization_time[request_type].append(int(row[ 8]))
    network_time        [request_type].append(int(row[ 9]))


print "request_type,response_time,std,ci95,serialization_time,std,ci95,deserialization_time,std,ci95,network_time,std,ci95"
for request_type in range(8):
    if request_type == 5 or request_type == 6: continue # skip login and logout messages

    sys.stdout.write(str(request_type) + ',')
    
    # print "confidence step"
#     if start == -1 or end == -1:
    discard = 0.1
    response_len = len(response_time[request_type]);
    serializ_len = len(serialization_time[request_type]);
    deserial_len = len(deserialization_time[request_type]);
    network__len = len(network_time[request_type]);

    response_time_array         = np.array(response_time          [request_type][int(response_len * discard): response_len - int(response_len * discard)])
    serialization_time_array    = np.array(serialization_time     [request_type][int(serializ_len * discard): serializ_len - int(serializ_len * discard)])
    deserialization_time_array  = np.array(deserialization_time   [request_type][int(deserial_len * discard): deserial_len - int(deserial_len * discard)])
    network_time_array          = np.array(network_time           [request_type][int(network__len * discard): network__len - int(network__len * discard)])
    
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

