from flask import Flask, request, jsonify
import easyocr
import io
from PIL import Image

app = Flask(__name__)
reader = easyocr.Reader(['ru', 'en'], gpu=True)

@app.route('/ocr', methods=['POST'])
def ocr_image():
    if 'file' not in request.files:
        return jsonify({"error": "no file"}), 400
    file = request.files['file']
    image = Image.open(io.BytesIO(file.read()))
    result = reader.readtext(image, detail=0)
    return jsonify({"text": " ".join(result)})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
