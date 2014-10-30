import matplotlib.pyplot as plt
import csv
import itertools
import math
import operator
import sys
import os
import datetime as dt
from datetime import datetime
import numpy as np
import scipy as sp
import scipy.stats
from math import sqrt

izip = itertools.izip

def read_middle_gc_log_values(prefix):
    exp_file=open(prefix + 'experiment.csv')
    next(exp_file)
    exp_count = 0
    db_query_start = []
    jvm_startup = []
    jvm_query_start = []
    for row in csv.reader(exp_file):
        db_query_start.append(datetime.strptime(row[1], '%Y-%m-%d %H:%M:%S.%f'))
        jvm_startup.append(datetime.strptime(row[2], '%Y-%m-%d %H:%M:%S.%f'))
        jvm_query_start.append(datetime.strptime(row[3], '%Y-%m-%d %H:%M:%S.%f'))
    
    min_db_query_start = db_query_start[0]
    
     
    gc_file=open(prefix + 'middle_time_gc_log.txt')
    gc_events = []
    prev_instance_id = ''
    exp_id = -1
    for line in gc_file:
        split = line.split(':');
        time = float(split[0])
        instance_id = split[2]
        if prev_instance_id != instance_id:
            prev_instance_id = instance_id
            exp_id += 1
            gc_events.append([])
        
#         print time
        time = time - (jvm_query_start[exp_id] - jvm_startup[exp_id]).total_seconds() + (db_query_start[exp_id] - min_db_query_start).total_seconds();
         
        gc_events[-1].append(time)
    
    return gc_events

print read_middle_gc_log_values(sys.argv[1])