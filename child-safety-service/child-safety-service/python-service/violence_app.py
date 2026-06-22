from flask import Flask, request, jsonify
from ultralytics import YOLO
import numpy as np
import cv2
import logging
import traceback
import tempfile
import os
import time
import base64
import gc
logging.basicConfig(level=logging.INFO)
app = Flask(__name__)
# ✅ CHANGE PORT MODEL HERE IF NEEDED
model = YOLO("yolo_small_weights.pt")
CONF_THRESHOLD = 0.5
BLOCK_THRESHOLD = 0.75
# ================= ERROR HANDLER =================
@app.errorhandler(Exception)
def handle_exception(e):
    logging.error(traceback.format_exc())
    return jsonify({
        "error": "server error",
        "details": str(e)
    }), 500
# ================= IMAGE =================
def decode_image(base64_str):
    if not base64_str:
        return None, "Empty image"
    if base64_str.startswith("data:image"):
        base64_str = base64_str.split(",")[1]
    try:
        img_bytes = base64.b64decode(base64_str, validate=True)
        np_arr = np.frombuffer(img_bytes, np.uint8)
        img = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
        return img, None
    except Exception as e:
        return None, str(e)
# ================= CORE PREDICTION =================
def predict_image(frame):
    results = model.predict(frame, verbose=False, conf=CONF_THRESHOLD)
    boxes = results[0].boxes
    detections = []
    if boxes is not None and len(boxes) > 0:
        for i in range(len(boxes)):
            xyxy = boxes.xyxy[i].tolist()
            conf = float(boxes.conf[i].item())
            cls = int(boxes.cls[i].item())
            label = "violence" if conf >= CONF_THRESHOLD else "non-violence"
            detections.append({
                "bbox": [
                    int(xyxy[0]),
                    int(xyxy[1]),
                    int(xyxy[2]),
                    int(xyxy[3])
                ],
                "class_id": cls,
                "confidence": conf,
                "label": label
            })
    return {
        "detections": detections,
        "total_boxes": len(detections)
    }
# ================= IMAGE ENDPOINT =================
@app.route("/predict", methods=["POST"])
def predict():
    data = request.get_json()
    if not data or "image" not in data:
        return jsonify({"error": "missing image"}), 400
    frame, error = decode_image(data["image"])
    if error:
        return jsonify({"error": error}), 400
    result = predict_image(frame)
    return jsonify(result)
# ================= VIDEO ENDPOINT =================
@app.route("/predict-video", methods=["POST"])
def predict_video():
    file = request.files.get("file")
    if not file:
        return jsonify({"error": "no file received"}), 400
    temp = tempfile.NamedTemporaryFile(delete=False, suffix=".mp4")
    path = temp.name
    temp.close()
    file.save(path)
    cap = cv2.VideoCapture(path)
    total_frames = 0
    unsafe_frames = 0
    all_detections = []
    try:
        while True:
            ret, frame = cap.read()
            if not ret:
                break
            total_frames += 1
            result = predict_image(frame)
            if result["total_boxes"] > 0:
                unsafe_frames += 1
            all_detections.append(result)
    finally:
        cap.release()
        gc.collect()
        time.sleep(1)
        try:
            os.remove(path)
        except:
            pass
    return jsonify({
        "total_frames": total_frames,
        "unsafe_frames": unsafe_frames,
        "detections_per_frame": all_detections
    })
# ================= FRAME ENDPOINT (raw bytes - WebSocket flow) =================
@app.route("/predict-frame", methods=["POST"])
def predict_frame():
    frame_bytes = request.data

    SAFE_RESULT = {
        "detected": False,
        "category": "NONE",
        "confidence": 0.0,
        "contentType": "IMAGE",
        "boundingBoxes": []
    }

    if not frame_bytes:
        return jsonify(SAFE_RESULT)

    nparr = np.frombuffer(frame_bytes, np.uint8)
    if nparr.size == 0:
        return jsonify(SAFE_RESULT)

    frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    if frame is None:
        return jsonify(SAFE_RESULT)

    result = predict_image(frame)

    # ✅ ADD THIS — see how many boxes the model actually found
    logging.info(f"Total detections: {result['total_boxes']}")
    for d in result["detections"]:
        logging.info(f"  bbox={d['bbox']} conf={d['confidence']:.2f}")

    detected = result["total_boxes"] > 0
    max_confidence = 0.0
    bounding_boxes = []

    if detected:
        for d in result["detections"]:
            conf = d["confidence"]
            if conf > max_confidence:
                max_confidence = conf
            x1, y1, x2, y2 = d["bbox"]
            bounding_boxes.append({
                "x": x1,
                "y": y1,
                "width":  x2 - x1,
                "height": y2 - y1
            })

    return jsonify({
        "detected": detected,
        "category": "VIOLENCE" if detected else "NONE",
        "confidence": round(max_confidence, 2),
        "contentType": "IMAGE",
        "boundingBoxes": bounding_boxes
    })
# ================= HEALTH =================
@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"})
# ================= RUN =================
if __name__ == "__main__":
    print("🚀 Flask running on port 5005")
    port = int(os.environ.get("PORT", 5005))
    app.run(host="0.0.0.0", port=port)