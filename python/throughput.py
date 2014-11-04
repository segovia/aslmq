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

bins = 1
time_step = 0.5 * 1000000000.0
elapsed_time = []
msg_count = [0]
for row in csv.reader(f):
    elapsed_time = int(row[3])
    if elapsed_time > bins * time_step:
        bins += 1
        msg_count.append(0)
    msg_count[-1] += 1


del msg_count[0]

steps = range(1,bins)
plt.plot(steps, msg_count, 'b')
plt.ylabel('Throughput (req/second)')
plt.xlabel('Elapsed seconds')
plt.xlim([1,bins-1])

# dt = datetime.datetime.now()
# plt.savefig('../gen/avg_resp_time.' + dt.strftime("%Y%m%d%H%M%S") + '.png', bbox_inches='tight')
plt.show()