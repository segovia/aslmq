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

def getThroughputAndResponseTimeArrayFromFile(filename):
    if filename[-4:] != '.csv': 
        exit(0)
    f=open(filename)
    next(f) # skip first line
    
    bins = 1
    seconds_per_step = 1.0
    time_step = seconds_per_step * sInNs
    response_time = []
    msg_count = [0]
    total_msg = 0
    for row in csv.reader(f):
        elapsed_time = int(row[3])
        request_type = int(row[5])
        if request_type < 0 or request_type > 7:
            raise NameError('Unknown request type')
        total_msg += 1
        if elapsed_time > bins * time_step:
            bins += 1
            msg_count.append(0)
        
        # id,experiment_id,client_id,elapsed_time,response_time,request_type,response_type,serialization_time,deserialization_time,network_time
        response_time.append(int(row[4]))
#         response_time.append(int(row[11]) + int(row[12]))
        msg_count[-1] += 1
    
    throughput_per_second           =   [c/seconds_per_step for c in msg_count];

    msgsToDiscard = int(total_msg*0.12)
    msgsToDiscardEnd = int(total_msg*0.05)
    curCount = 0
    curBin = 0
    start = 0
    end = bins
    for c in msg_count:
        if curCount < msgsToDiscard:
            start = curBin + 1
        if curCount + c <= total_msg-msgsToDiscardEnd:
            end = curBin + 1
        curBin += 1
        curCount += c
    
    throughput_per_second               = throughput_per_second             [start:end]
    response_time = response_time[msgsToDiscard:total_msg-msgsToDiscard]
    return [response_time, throughput_per_second]

table_for_23_design = [[  1, -1, -1, -1,  1,  1,  1, -1],
                       [  1,  1, -1, -1, -1, -1,  1,  1],
                       [  1, -1,  1, -1, -1,  1, -1,  1],
                       [  1,  1,  1, -1,  1, -1, -1, -1],
                       [  1, -1, -1,  1,  1, -1, -1,  1],
                       [  1,  1, -1,  1, -1,  1, -1, -1],
                       [  1, -1,  1,  1, -1, -1,  1, -1],
                       [  1,  1,  1,  1,  1,  1,  1,  1]]

# response_times = [np.array([14.0,16.0,12.0]),
#                   np.array([22.0,18.0,20.0]),
#                   np.array([11.0,15.0,19.0]),
#                   np.array([34.0,30.0,35.0]),
#                   np.array([46.0,42.0,44.0]),
#                   np.array([58.0,62.0,60.0]),
#                   np.array([50.0,55.0,54.0]),
#                   np.array([86.0,80.0,74.0])]
# throughputs    = response_times

response_times = []
throughputs    = []
for i in range(1,9):
    vals = getThroughputAndResponseTimeArrayFromFile(sys.argv[i])
    response_times.append(np.array(vals[0])/msInNs)
    throughputs   .append(np.array(vals[1]))
    print "file " + str(i) + " read"

response_time_repetitions = len(response_times[0])
throughput_repetitions = len(throughputs[0])
for i in range(1,8):
    response_time_repetitions = min(response_time_repetitions, len(response_times[i]))
    throughput_repetitions    = min(throughput_repetitions,    len(throughputs   [i]))
    
    

# make sure all have same amount of repetitions
for i in range(8):
    response_times[i] = response_times[i][0:response_time_repetitions]
    throughputs   [i] = throughputs   [i][0:throughput_repetitions]
    

response_time_means = np.array([0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0])
throughput_means    = np.array([0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0])
response_time_residuals = []
throughput_residuals    = []

for i in range(8):
    response_time_means[i] = np.mean(response_times[i])
    throughput_means   [i] = np.mean(throughputs   [i])
    response_time_residuals.append(response_times[i] - response_time_means[i])
    throughput_residuals   .append(throughputs   [i] - throughput_means   [i])
    
response_time_effects = np.array([0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0])
throughput_effects    = np.array([0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0])
for i in range(8):
    for j in range(8):
        response_time_effects[i] += (table_for_23_design[j][i] * response_time_means[j]) / 8.0
        throughput_effects   [i] += (table_for_23_design[j][i] * throughput_means   [j]) / 8.0

response_time_effect_errors = np.array([0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0])
throughput_effect_errors    = np.array([0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0])
for i in range(8):
    response_time_effect_errors[i]  = 8 * response_time_repetitions * response_time_effects[i] * response_time_effects[i]
    throughput_effect_errors   [i]  = 8 * throughput_repetitions * throughput_effects   [i] * throughput_effects   [i]

response_time_SSY  = 0
response_time_SSE  = 0
throughput_SSY  = 0
throughput_SSE  = 0
for i in range(8):
    response_time_SSY += response_times[i].dot(response_times[i])
    response_time_SSE += response_time_residuals[i].dot(response_time_residuals[i])
    throughput_SSY    += throughputs[i].dot(throughputs[i])
    throughput_SSE    += throughput_residuals[i].dot(throughput_residuals[i])

response_time_SSE2 = response_time_SSY - 8 * response_time_repetitions * (response_time_effects.dot(response_time_effects))
throughput_SSE2    = throughput_SSY    - 8 * throughput_repetitions    * (throughput_effects.dot(throughput_effects))
response_time_SST1 = response_time_SSY - response_time_effect_errors[0]
throughput_SST1    = throughput_SSY -    throughput_effect_errors[0]
response_time_SST2 = response_time_effect_errors[1] + response_time_effect_errors[2] + response_time_effect_errors[3] + response_time_SSE
throughput_SST2    = throughput_effect_errors[1]    + throughput_effect_errors[2]    + throughput_effect_errors[3]    + throughput_SSE

response_time_std        = sqrt(response_time_SSE/(8 * (response_time_repetitions - 1)))
response_time_effect_std = response_time_std / sqrt(8 * response_time_repetitions)
throughput_std           = sqrt(throughput_SSE/(8 * (throughput_repetitions - 1)))
throughput_effect_std    = throughput_std / sqrt(8 * throughput_repetitions)
response_time_ci90 = sp.stats.t.ppf((1+0.90)/2., 8 * (response_time_repetitions - 1))
response_time_ci95 = sp.stats.t.ppf((1+0.95)/2., 8 * (response_time_repetitions - 1))
throughput_ci90 = sp.stats.t.ppf((1+0.90)/2., 8 * (throughput_repetitions - 1))
throughput_ci95 = sp.stats.t.ppf((1+0.95)/2., 8 * (throughput_repetitions - 1))

print "response time repetitions: " + str(response_time_repetitions)
print "mean response times: " + str(response_time_means)
print "mean response time effects: " + str(response_time_effects)
print "response_time vals"
print "SSY: " + str(response_time_SSY)
print "SS0: " +  str(response_time_effect_errors[0])
print "SSA: " +  str(response_time_effect_errors[1])
print "SSB: " +  str(response_time_effect_errors[2])
print "SSC: " +  str(response_time_effect_errors[3])
print "SSAB: " + str(response_time_effect_errors[4])
print "SSAC: " + str(response_time_effect_errors[5])
print "SSBC: " + str(response_time_effect_errors[6])
print "SSABC: "+ str(response_time_effect_errors[7])
print "SST1: " + str(response_time_SST1)
print "SST2: " + str(response_time_SST2)
print "SSE: " + str(response_time_SSE)
print "SSE2: " + str(response_time_SSE2)
print "var A: %.2f%%"   % (response_time_effect_errors[1] * 100.0 / response_time_SST1)
print "var B: %.2f%%"   % (response_time_effect_errors[2] * 100.0 / response_time_SST1)
print "var C: %.2f%%"   % (response_time_effect_errors[3] * 100.0 / response_time_SST1)
print "var AB: %.2f%%"  % (response_time_effect_errors[4] * 100.0 / response_time_SST1)
print "var AC: %.2f%%"  % (response_time_effect_errors[5] * 100.0 / response_time_SST1)
print "var BC: %.2f%%"  % (response_time_effect_errors[6] * 100.0 / response_time_SST1)
print "var ABC: %.2f%%" % (response_time_effect_errors[7] * 100.0 / response_time_SST1)
print "std: " + str(response_time_std)
print "effect_std: " + str(response_time_effect_std)
print "ci95: " + str(throughput_ci95)
print "ci*effect_std/effect: " + str(response_time_effect_std * response_time_ci90 / response_time_effects)

print ""
print "throughput vals"
print "throughput repetitions: " + str(throughput_repetitions)
print "mean throughputs: " + str(throughput_means)
print "mean throughput effects: " + str(throughput_effects)
print "SSY: " + str(throughput_SSY)
print "SS0: " + str(throughput_effect_errors[0])
print "SSA: " + str(throughput_effect_errors[1])
print "SSB: " + str(throughput_effect_errors[2])
print "SSC: " + str(throughput_effect_errors[3])
print "SSAB: " + str(throughput_effect_errors[4])
print "SSAC: " + str(throughput_effect_errors[5])
print "SSBC: " + str(throughput_effect_errors[6])
print "SSABC: "+ str(throughput_effect_errors[7])
print "SST1: " + str(throughput_SST1)
print "SST2: " + str(throughput_SST2)
print "SSE: " + str(throughput_SSE)
print "SSE: " + str(throughput_SSE2)
print "var A: %.2f%%"   % (throughput_effect_errors[1] * 100.0 / throughput_SST1)
print "var B: %.2f%%"   % (throughput_effect_errors[2] * 100.0 / throughput_SST1)
print "var C: %.2f%%"   % (throughput_effect_errors[3] * 100.0 / throughput_SST1)
print "var AB: %.2f%%"  % (throughput_effect_errors[4] * 100.0 / throughput_SST1)
print "var AC: %.2f%%"  % (throughput_effect_errors[5] * 100.0 / throughput_SST1)
print "var BC: %.2f%%"  % (throughput_effect_errors[6] * 100.0 / throughput_SST1)
print "var ABC: %.2f%%" % (throughput_effect_errors[7] * 100.0 / throughput_SST1)
print "std: " + str(throughput_std)
print "effect_std: " + str(throughput_effect_std)
print "ci95: " + str(throughput_ci95)
print "ci*effect_std/effect: " + str(throughput_effect_std * response_time_ci90 / throughput_effects)


# mean = np.mean(response_time_array)
# std = np.std(response_time_array)
# ci95 = scipy.stats.sem(response_time_array) * sp.stats.t.ppf((1+0.95)/2., len(response_time_array)-1) # sem =  standard error of the mean => std/sqrt(len(response_time_array))
# 
# print "response time for csv: " + str(len(response_time_array)) + "," + str(mean) + "," + str(std) + "," + str(ci95)
# # print("%.2f%%" % (ci95 * 100.0 / mean))
# 
# 
# tp_mean = np.mean(throughput_array)
# tp_std = np.std(throughput_array)
# tp_ci95 = scipy.stats.sem(throughput_array) * sp.stats.t.ppf((1+0.95)/2., len(throughput_array)-1) # sem =  standard error of the mean => std/sqrt(len(response_time_array))
#         
# print "throughput for csv: " + str(len(throughput_array)) + "," + str(tp_mean) + "," + str(tp_std) + "," + str(tp_ci95)
# # print("%.2f%%" % (tp_ci95 * 100.0 / tp_mean))
        
        
        
        
        
        
        
