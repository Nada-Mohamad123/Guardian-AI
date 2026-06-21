import cv2
from ultralytics import YOLO
import os

# ===== إعداد المسارات =====
VIDEO_INPUT = "videos/input_video.mp4"       # الفيديو اللي عايزة تحليله
VIDEO_OUTPUT = "output/output_violence.mp4"  # الفيديو النهائي
WEIGHTS = "yolo_small_weights.pt"            # استخدمي Nano أو Small

CONF_THRESHOLD = 0.5  # أقل confidence تظهر به "Violence"

# تأكد أن فولدر output موجود
os.makedirs("output", exist_ok=True)

# ===== تحميل موديل YOLOv8 =====
model = YOLO(WEIGHTS)

# ===== فتح الفيديو =====
cap = cv2.VideoCapture(VIDEO_INPUT)
width  = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
fps    = cap.get(cv2.CAP_PROP_FPS)

fourcc = cv2.VideoWriter_fourcc(*"mp4v")
out = cv2.VideoWriter(VIDEO_OUTPUT, fourcc, fps, (width, height))

# ===== متغيرات العد =====
frame_count = 0
violence_count = 0
nonviolence_count = 0

# ===== معالجة كل فريم =====
while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        break

    frame_count += 1

    # YOLO inference
    results = model.predict(frame, verbose=False)
    boxes = results[0].boxes

    # افتراض Non-Violence
    label_text = "Non-Violence"
    color = (0,255,0)
    confidence_score = 0.0

    # التحقق من أي كشف و confidence > threshold
    if len(boxes) > 0:
        # أعلى confidence في الفريم
        max_conf = boxes.conf.max().item()
        confidence_score = max_conf
        if max_conf > CONF_THRESHOLD:
            label_text = "Violence"
            color = (0,0,255)

    # رسم الصناديق فقط
    for box in boxes.xyxy:
        x1, y1, x2, y2 = map(int, box)
        cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)

    # كتابة النص الرئيسي فوق الفريم مع confidence
    cv2.putText(frame, f"{label_text} ({confidence_score:.2f})", (20, 40),
                cv2.FONT_HERSHEY_SIMPLEX, 1.0, color, 2)

    # تحديث العد
    if label_text == "Violence":
        violence_count += 1
    else:
        nonviolence_count += 1

    # حفظ الفريم
    out.write(frame)

    # طباعة كل 10 فريمات
    if frame_count % 10 == 0:
        print(f"Processed {frame_count} frames...")

cap.release()
out.release()

print("✅ Done!")
print(f"Total frames: {frame_count}")
print(f"Frames with Violence: {violence_count}")
print(f"Frames with Non-Violence: {nonviolence_count}")
print(f"Video saved at: {VIDEO_OUTPUT}")
