from random import random
from datetime import datetime

import numpy as np

dist = np.linspace(0, 23, 24)
dist = list(map(np.sin, dist))


def shift(data, offset):
    hour = datetime.now().hour
    hour += offset
    hour %= 24
    return [data[0], data[1], data[2] + random() * dist[hour], data[3] + random() * dist[hour]]
