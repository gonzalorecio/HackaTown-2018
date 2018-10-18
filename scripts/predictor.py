from build_predictor import build
from keras.models import load_model


def predict_data(predictor):
    return [1, 2, 3]


def predict():
    try:
        predictor = load_model('predictor.h5')
        return predict_data(predictor)
    except OSError:
        print('Predictor not found')
        build()
        try:
            predictor = load_model('predictor.h5')
            return predict_data(predictor)
        except OSError:
            return 'Can\'t predict data', 500
