import matplotlib.pyplot as plt
import csv
import os
import itertools
import math
import operator
import sys
import datetime as dt
from datetime import datetime
izip = itertools.izip

out=open(sys.argv[1], 'w')
for filename in sys.argv[1:]:
    f=open(filename)
    for line in f:
        out.write(line[0:-1] + ' : ' + os.path.basename(filename) + '\n')
