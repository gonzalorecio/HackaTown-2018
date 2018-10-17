import numpy as np
from scipy import interpolate


def generate_data():
    """
    Generate data to feed the interpolator
    :return: independent variable, latitude and longitude
    """
    data = [[41.1, 2.1, 10], [41.1, 2.5, 15]]
    data.sort(key=lambda el: el[-1])
    data = np.array(data)
    return data[:, -1], data[:, 0], data[:, 1]


def interpolate_data():
    cont, lat, lng = generate_data()
    interpolator = interpolate.Rbf(lat, lng, cont)
    print(interpolator(41.1, 2.3))


if __name__ == '__main__':
    interpolate_data()
