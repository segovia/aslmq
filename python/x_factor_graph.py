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
from scipy import interpolate

izip = itertools.izip

msInNs = 1000000.0
sInNs =  1000000000.0

# print "step -1"
prefix = sys.argv[1]
title = sys.argv[2]
suffix = sys.argv[3]
f=open(prefix + 'response_time.csv')
next(f) # skip first line

#label,samples,response time/throughput,std,ci95,ci99,errors,ci95%
response_time_label = []
response_time = []
response_time_std = []
response_time_ci95 = []

for row in csv.reader(f):
    if len(row) == 0: continue;
    response_time_label.append(row[0])
    response_time.append(float(row[2])/msInNs)
    response_time_std.append(float(row[3])/msInNs)
    response_time_ci95.append(float(row[4])/msInNs)

f=open(prefix + 'throughput.csv')
next(f) # skip first line
throughput_label = []
throughput = []
throughput_std = []
throughput_ci95 = []
for row in csv.reader(f):
    if len(row) == 0: continue;
    throughput_label.append(row[0])
    throughput.append(float(row[2]))
    throughput_std.append(float(row[3]))
    throughput_ci95.append(float(row[4]))
    
for i in range(len(response_time)):
    if throughput_label[i] != response_time_label[i]: raise NameError('Labels are not matching: ' + throughput_label[i] + ', ' + response_time_label[i])

    
ind = np.arange(1)
width = 0.25
opacity = 0.4

colors = ['blue', 'green', 'red', 'orange', 'cyan', 'magenta', 'yellow', 'black']


plt.figure(figsize =(10,3.5))
plt.subplot2grid((1,2), (0,0), rowspan=1)
plt.subplots_adjust(wspace=.3)

for i in range(len(response_time)):
    plt.bar(i*width, response_time[i], width, alpha=opacity, color=colors[i])
    plt.errorbar((i+0.5)*width, response_time[i], yerr=response_time_std[i], ls='None', color="k", capsize=8)
    plt.errorbar((i+0.5)*width, response_time[i], yerr=response_time_ci95[i], ls='None', color="r", capsize=8)
plt.ylim(0)
plt.ylabel("(ms)")
plt.xlabel("response time mean")
plt.gca().xaxis.set_major_locator(plt.NullLocator())

plt.subplot2grid((1,2), (0,1), rowspan=1)
for i in range(len(response_time)):
    plt.bar(i*width, throughput[i], width, alpha=opacity, color=colors[i], label=throughput_label[i])
    plt.errorbar((i+0.5)*width, throughput[i], yerr=throughput_std[i], ls='None', color="k", capsize=8, label=('standard deviation' if i + 1 == len(response_time) else ''))
    plt.errorbar((i+0.5)*width, throughput[i], yerr=throughput_ci95[i], ls='None', color="r", capsize=8, label=('95% confidence interval' if i + 1 == len(response_time) else ''))
plt.legend(loc='upper center', bbox_to_anchor=(-0.15, -0.1), ncol=2, numpoints=1, prop={'size':10})
plt.ylim(0)
plt.ylabel("(msg/s)")
plt.xlabel("throughput mean")
plt.gca().xaxis.set_major_locator(plt.NullLocator())
plt.suptitle(title, fontsize=14)

plt.savefig('gen/' + os.path.basename(prefix)[0:-1] + '.' + suffix + '.png', bbox_inches='tight', dpi = 200)

# plt.show()