import numpy as np
from scipy import interpolate
from shift_data import shift


def preprocess_data(offset):
    """
    Generate data to feed the interpolator
    :return: independent variable, latitude and longitude
    """
    # Canviar per sensors reals
    data = [[45.789845, 15.916228, 3.0, 3.0], [45.82069, 15.979998, 4.0, 39.0], [45.818, 15.961325, 1.0, 1.0],
            [45.810226, 15.97455, 16.0, 1.0], [45.799343, 15.966839, 1.0, 1.0],
            [45.82371520996094, 16.035825729370117, 53.2, 14.2]]
    data = list(map(lambda d: shift(d, offset), data))
    no2 = sorted(data, key=lambda el: el[2])
    pm10 = sorted(data, key=lambda el: el[3])

    no2 = np.array(no2)
    pm10 = np.array(pm10)
    return (no2[:, 0], no2[:, 1], no2[:, 2]), (pm10[:, 0], pm10[:, 1], pm10[:, 3])


def interpolate_data(min_lat, min_lng, max_lat, max_lng, steps, offset):
    (lats1, longs1, no2), (lats2, longs2, pm10) = preprocess_data(offset)
    interpolator_no2 = interpolate.Rbf(lats1, longs1, no2)
    interpolator_pm10 = interpolate.Rbf(lats2, longs2, no2)

    y = np.linspace(min_lat, max_lat, num=steps)
    x = np.linspace(min_lng, max_lng, num=steps)
    grid_x, grid_y = np.meshgrid(y, x, sparse=False)

    response = []
    for i in range(len(grid_x)):
        for j in range(len(grid_x[0])):
            lat = grid_x[i, j]
            lng = grid_y[i, j]
            no2 = interpolator_no2(lat, lng)
            pm10 = interpolator_pm10(lat, lng)
            response.append({
                'lat': lat,
                'lng': lng,
                'no2': no2.tolist(),
                'pm10': pm10.tolist()
            })
    return response
