from datetime import datetime

import pandas as pd
from keras import Sequential
from keras.layers import LSTM, Dense
from matplotlib import pyplot as plt
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler, LabelEncoder

TIMESTEPS = 14
EPOCHS = 20
BATCH_SIZE = 32
FEATURES = 8


def preprocess_dataset():
    dataset = pd.read_csv('dataset_1.csv', parse_dates=[['year', 'month', 'day', 'hour']], index_col=0,
                          date_parser=lambda d: datetime.strptime(d, '%Y %m %d %H'))
    dataset.drop('NO', axis=1, inplace=True)
    dataset.columns = ['pollution', 'dew', 'temp', 'press', 'wnd_dir', 'wnd_spd', 'snow', 'rain']
    dataset.index.name = 'date'

    plt.figure()
    for group in range(0, 7):
        plt.subplot(7, 1, group + 1)
        plt.plot(dataset.values[:, group])
        plt.title(dataset.columns[group], y=0.5, loc='right')
    plt.show()

    return dataset


def series_to_supervised(data, n_in=1, n_out=1, drop_nan=True):
    n_vars = data.shape[1]
    df = pd.DataFrame(data)
    cols, names = [], []
    # Input sequence
    for i in range(n_in, 0, -1):
        cols.append(df.shift(i))
        names += ['var{}(t-{})'.format(j + 1, i) for j in range(n_vars)]
    # Output sequence
    cols.append(df.shift(0))
    names += ['var{}(t)'.format(j + 1) for j in range(n_vars)]
    for i in range(1, n_out):
        cols.append(df.shift(-i))
        names += ['var{}(t+{})'.format(j + 1, i) for j in range(n_vars)]

    df = pd.concat(cols, axis=1)
    df.columns = names
    if drop_nan:
        df.dropna(inplace=True)
    return df


def scale_data(dataset):
    encoder = LabelEncoder()
    sc = MinMaxScaler(feature_range=(0, 1))

    values = dataset.values
    values[:, 4] = encoder.fit_transform(values[:, 4])
    values = values.astype('float32')
    scaled = sc.fit_transform(values)
    return series_to_supervised(scaled, TIMESTEPS, 1)


def split_dataset(dataset):
    n_obs = TIMESTEPS * FEATURES
    values = dataset.values
    x_data = values[:, :n_obs]
    y_data = values[:, -n_obs]
    x_train, x_test, y_train, y_test = train_test_split(x_data, y_data, test_size=0.2)

    # Reshape into (samples, timesteps, features)
    x_train = x_train.reshape((x_train.shape[0], TIMESTEPS, FEATURES))
    x_test = x_test.reshape((x_test.shape[0], TIMESTEPS, FEATURES))
    print(x_train.shape, y_train.shape, x_test.shape, y_test.shape)
    return x_train, y_train, x_test, y_test


def build_predictor(shapes):
    predictor = Sequential()
    predictor.add(LSTM(units=50, return_sequences=True, input_shape=(shapes[0], shapes[1])))
    predictor.add(LSTM(units=50, return_sequences=True))
    predictor.add(LSTM(units=50))
    predictor.add(Dense(units=1))
    predictor.compile(optimizer='adam', loss='mean_squared_error')
    return predictor


def fit_predictor(predictor, x_train, y_train, x_test, y_test):
    history = predictor.fit(x_train, y_train, epochs=EPOCHS, batch_size=BATCH_SIZE,
                            validation_data=(x_test, y_test), verbose=2, shuffle=False)

    plt.plot(history.history['loss'], label='train')
    plt.plot(history.history['val_loss'], label='test')
    plt.legend()
    plt.show()


def build():
    dataset = preprocess_dataset()
    dataset = scale_data(dataset)
    x_train, y_train, x_test, y_test = split_dataset(dataset)
    predictor = build_predictor((x_train.shape[1], x_train.shape[2]))
    fit_predictor(predictor, x_train, y_train, x_test, y_test)


build()
