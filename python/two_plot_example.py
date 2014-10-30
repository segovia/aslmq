import matplotlib.pyplot as plt
import csv
import itertools
import math
import operator

import datetime


izip = itertools.izip

f=open("../measurements/test2.csv")
next(f) # skip first line
elapsed_time = []
response_time = []
for row in csv.reader(f):
    elapsed_time.append(int(row[3]))
    response_time.append(int(row[4]))

time_step = 1000.0
bins = int(math.ceil(elapsed_time[-1]/time_step))+1

response_time_sum = [0] * bins
msg_count = [0] * bins


for e,r in izip(elapsed_time,response_time):
    index = int(math.ceil(e/time_step))
    response_time_sum[index] += r
    msg_count[index] += 1
    
average_response_time = [0.0 if c == 0 else s/c for s, c in izip(response_time_sum, msg_count)];
del average_response_time[0]
del msg_count[0]


steps = range(1,bins)

# plt.plot(steps, average_response_time, 'b', msg_count, 'r')
# plt.ylabel('Average response time (per second)')
# plt.xlabel('Elapsed seconds')

fig, ax1 = plt.subplots()
ax1.plot(steps, average_response_time, 'b')
ax1.set_xlabel('Elapsed seconds')
# Make the y-axis label and tick labels match the line color.
ax1.set_ylabel('Average response time (per second)', color='b')
for tl in ax1.get_yticklabels():
    tl.set_color('b')


ax2 = ax1.twinx()
ax2.plot(steps, msg_count, 'r')
ax2.set_ylabel('Throughput (msg/second)', color='r')
for tl in ax2.get_yticklabels():
    tl.set_color('r')


# dt = datetime.datetime.now()
# plt.savefig('../gen/avg_resp_time.' + dt.strftime("%Y%m%d%H%M%S") + '.png', bbox_inches='tight')
plt.show()