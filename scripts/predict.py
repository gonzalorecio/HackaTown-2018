from keras import Sequential
from keras.layers import LSTM, Dense

predictor = Sequential()

predictor.add(LSTM(units=3, return_sequences=True, input_shape=(None, 1)))
predictor.add(LSTM(units=3, return_sequences=True))
predictor.add(LSTM(units=3, return_sequences=True))
predictor.add(LSTM(units=3))
predictor.add(Dense(units=1))

predictor.compile(optimizer='rmsprop', loss='mean_squared_error')