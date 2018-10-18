from random import random
from datetime import datetime

import numpy as np

dist = np.linspace(0, 23, 24)
dist = list(map(lambda d: np.sin(d/10), dist))


def shift(data, offset):
    hour = datetime.now().hour
    hour += offset
    hour %= 24
    return [data[0], data[1], data[2] + random()*5 * dist[hour], data[3] + random()*5 * dist[hour]]
