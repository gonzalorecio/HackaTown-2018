from flask_cors import CORS
from flask import Flask, request, jsonify
from interpolate import interpolate_data

app = Flask(__name__)
CORS(app)


@app.route('/', methods=['GET'])
def get_data():
    min_lat = request.args.get('min_lat', type=float)
    min_lng = request.args.get('min_lng', type=float)
    max_lat = request.args.get('max_lat', type=float)
    max_lng = request.args.get('max_lng', type=float)
    if None in [min_lat, min_lng, max_lat, max_lng]:
        return 'Missing parameters', 400

    steps = request.args.get('steps', 10, type=int)
    offset = request.args.get('offset', 0, type=int)

    response = interpolate_data(min_lat, min_lng, max_lat, max_lng, steps, offset)
    return jsonify(response)


if __name__ == '__main__':
    app.run(port=8080)
