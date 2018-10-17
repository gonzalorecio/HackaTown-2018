import numpy as np
from scipy import interpolate


def preprocess_data():
    """
    Generate data to feed the interpolator
    :return: independent variable, latitude and longitude
    """
    data = [[45.849, 15.836, 50], [45.74, 16.0809, 20]]
    data.sort(key=lambda el: el[-1])
    data = np.array(data)
    return data[:, -1], data[:, 0], data[:, 1]


def interpolate_data(min_lat, min_lng, max_lat, max_lng, steps):
    weights, lats, longs = preprocess_data()
    interpolator = interpolate.Rbf(lats, longs, weights)

    y = np.linspace(min_lat, max_lat, num=steps)
    x = np.linspace(min_lng, max_lng, num=steps)
    grid_x, grid_y = np.meshgrid(y, x, sparse=False)

    response = []
    for i in range(len(grid_x)):
        for j in range(len(grid_x[0])):
            lat = grid_x[i, j]
            lng = grid_y[i, j]
            value = interpolator(lat, lng)
            response.append({
                'lat': lat,
                'lng': lng,
                'weight': value.tolist()
            })
    return response
