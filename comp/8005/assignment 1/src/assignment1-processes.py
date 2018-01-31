from threading import Thread
import time
import logging
from decimal import *
import decimal
import timeit
import os

logging.basicConfig(level=logging.DEBUG, format='(%(threadName)s) %(message)s', )

# Note: this method taken from 
# https://docs.python.org/3/library/decimal.html#recipes
def pi():
    """Compute Pi to the current precision.

    >>> print(pi())
    3.141592653589793238462643383

    """
    getcontext().prec += 2  # extra digits for intermediate steps
    three = Decimal(3)      # substitute "three=3.0" for regular floats
    lasts, t, s, n, na, d, da = 0, three, 3, 1, 0, 0, 24
    while s != lasts:
        lasts = s
        n, na = n+na, na+8
        d, da = d+da, da+32
        t = (t * n) / d
        s += t
    getcontext().prec -= 2
    return +s               # unary plus applies the new precision. 


def calculate():
	decimal.getcontext().prec = 30000
	start_time = timeit.default_timer()
	logging.info('starting execution %s', os.getpid())
	pi()
	elapsed = timeit.default_timer() - start_time
	logging.info('time taken %s seconds %s', elapsed, os.getpid())

start_time = timeit.default_timer()
forks = 4
children = []

for i in range(forks):
    pid = os.fork()
    if pid:
        children.append(pid)
    else:
        calculate()
        os._exit(0)
for i, child in enumerate(children):
    os.waitpid(child, 0)
elapsed = timeit.default_timer() - start_time
logging.info('program time taken %s seconds', elapsed)