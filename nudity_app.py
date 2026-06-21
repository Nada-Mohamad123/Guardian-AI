from flask import Flask, request, jsonify
from nudenet import NudeDetector
import base64
import numpy as np
import cv2
import tempfile
import os
import time
import gc

app = Flask(__name__)
detector = NudeDetector()

# ================= IMAGE DECODER =================
def decode_image(base64_str):
    try:
        if not base64_str:
            return None

        if base64_str.startswith("data:image"):
            base64_str = base64_str.split(",")[1]

        # fix base64 padding
        missing_padding = len(base64_str) % 4
        if missing_padding:
            base64_str += "=" * (4 - missing_padding)

        img_bytes = base64.b64decode(base64_str, validate=True)
        np_arr = np.frombuffer(img_bytes, np.uint8)
        img = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)

        return img

    except Exception as e:
        print("Decode error:", e)
        return None


# ================= IMAGE ENDPOINT =================
@app.route("/predict-nudity", methods=["POST"])
def predict_nudity():

    data = request.get_json()

    if not data or "image" not in data:
        return jsonify({
            "is_safe": False,
            "reason": "missing image",
            "confidence": 0.0
        }), 400

    frame = decode_image(data["image"])

    if frame is None:
        return jsonify({
            "is_safe": False,
            "reason": "invalid image",
            "confidence": 0.0
        }), 400

    detections = detector.detect(frame)

    is_nude = len(detections) > 0
    confidence = max([d["score"] for d in detections], default=0.0)

    return jsonify({
        "is_safe": not is_nude,
        "reason": "Nudity detected" if is_nude else "Clean image",
        "confidence": round(confidence, 2)
    })


# ================= VIDEO ENDPOINT =================
@app.route("/predict-nudity-video", methods=["POST"])
def predict_nudity_video():

    print("===== VIDEO ENDPOINT HIT =====")
    print("Files:", request.files)
    print("Content-Type:", request.content_type)

    if not request.files:
        return jsonify({
            "is_safe": False,
            "reason": "no files in request",
            "confidence": 0.0
        }), 400

    file = request.files.get("file")

    if file is None:
        return jsonify({
            "is_safe": False,
            "reason": "no file received (use key=file)",
            "confidence": 0.0
        }), 400

    if file.filename == "":
        return jsonify({
            "is_safe": False,
            "reason": "empty file",
            "confidence": 0.0
        }), 400

    # Save temp file
    temp = tempfile.NamedTemporaryFile(delete=False, suffix=".mp4")
    path = temp.name
    temp.close()

    file.save(path)
    print(f"File saved: {path}")

    cap = cv2.VideoCapture(path)

    total_frames = 0
    unsafe_frames = 0
    max_confidence = 0.0
    frame_count = 0

    try:
        while True:
            ret, frame = cap.read()
            if not ret:
                break

            frame_count += 1

            # Skip frames (performance)
            if frame_count % 5 != 0:
                continue

            total_frames += 1

            results = detector.detect(frame)

            confidence = max(
                [r["score"] for r in results],
                default=0.0
            )

            if confidence > 0.5:
                unsafe_frames += 1

            max_confidence = max(max_confidence, confidence)

    except Exception as e:
        print("ERROR:", e)

    finally:
        cap.release()

        # cleanup
        gc.collect()
        time.sleep(1)

        try:
            os.remove(path)
            print("Temp file deleted")
        except:
            pass

    return jsonify({
        "total_frames": total_frames,
        "unsafe_frames": unsafe_frames,
        "is_safe": unsafe_frames == 0,
        "confidence": round(max_confidence, 2)
    })


# ================= HEALTH CHECK =================
@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "ok",
        "service": "nudity-detection"
    })


# ================= RUN =================
if __name__ == "__main__":
    print("Nudity Service Running on port 6000...")
    app.run(host="0.0.0.0", port=6000, debug=True)