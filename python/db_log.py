import matplotlib.pyplot as plt
import csv
import itertools
import math
import operator
import sys
import datetime as dt
from datetime import datetime
izip = itertools.izip

f=open(sys.argv[1])

start_time = dt.datetime(2014,01,01)
chk_start = dt.datetime(2014,01,01)
for line in f:
    if line[0:1] == '\t': continue
    time = datetime.strptime(line[0:27], '%Y-%m-%d %H:%M:%S.%f %Z')
    if 'database system is ready to accept connections' in line:
        print str(time) + ' -> start'
        start_time = time
    if 'checkpoint starting' in line:
        chk_start = time
    if 'checkpoint complete' in line:
        print line[0:27] + '   elapsed: ' +  str((time - start_time).seconds+20) + ' - seconds: ' + str((time - chk_start).seconds)
#         print str(time) + ' -> ' + line[27:-1]
#     if 'automatic vacuum' in line:
#         print 'elapsed: ' +  str((time - start_time).seconds+20) + '\t' + line[27:-1]

