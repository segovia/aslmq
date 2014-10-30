import matplotlib.pyplot as plt
import csv
import itertools
import math
import operator
import sys
import datetime
izip = itertools.izip

f=open(sys.argv[1])
next(f) # skip first line
elapsed_time = []
response_time = []
for row in csv.reader(f):
    elapsed_time.append(int(row[3]))
    response_time.append(int(row[4]))

time_step = 1000000000.0
bins = int(math.ceil((elapsed_time[-1] - elapsed_time[0])/time_step))+1

response_time_sum = [0] * bins
msg_count = [0] * bins


for e,r in izip(elapsed_time,response_time):
    index = int(math.ceil((e - elapsed_time[0])/time_step))
    response_time_sum[index] += r
    msg_count[index] += 1
    
average_response_time = [0.0 if c == 0 else s/(c*1000000.0) for s, c in izip(response_time_sum, msg_count)];
del average_response_time[0]
del msg_count[0]

steps = range(1,bins)

plt.plot(steps, average_response_time, 'b')
plt.ylabel('Average response time (ms/second)')
plt.xlabel('Elapsed seconds')
plt.xlim([1,bins-1])

# dt = datetime.datetime.now()
# plt.savefig('../gen/avg_resp_time.' + dt.strftime("%Y%m%d%H%M%S") + '.png', bbox_inches='tight')
plt.show()