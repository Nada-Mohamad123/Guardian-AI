from fastapi import FastAPI, Request
import cv2
import numpy as np
import mediapipe as mp
from typing import List

app = FastAPI()

mp_pose = mp.solutions.pose
pose = mp_pose.Pose(static_image_mode=True, min_detection_confidence=0.5)

LOWER = np.array([0, 40, 70], dtype=np.uint8)
UPPER = np.array([25, 170, 255], dtype=np.uint8)

SKIN_RATIO_THRESHOLD = 0.30
BLOCK_THRESHOLD = 0.70

SAFE_RESULT = {
    "detected": False,
    "category": "NONE",
    "confidence": 0.0,
    "action": "ALLOWED",
    "contentType": "IMAGE",
    "boundingBoxes": []
}


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/analyze")
async def analyze(request: Request):
    frame_bytes = await request.body()

    if not frame_bytes:
        print("ERROR: Empty body received")
        return SAFE_RESULT

    nparr = np.frombuffer(frame_bytes, np.uint8)

    if nparr.size == 0:
        print("ERROR: Empty numpy array")
        return SAFE_RESULT

    frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    if frame is None:
        print("ERROR: Could not decode image — bytes may not be a valid image")
        return SAFE_RESULT

    h, w, _ = frame.shape
    print(f"✓ Frame decoded: {w}x{h} pixels, {len(frame_bytes)} bytes")

    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    skin_mask = cv2.inRange(hsv, LOWER, UPPER)

    raw_skin_area = cv2.countNonZero(skin_mask)
    raw_ratio = raw_skin_area / (h * w)
    print(f"✓ Raw skin ratio (before morphology): {raw_ratio:.3f}")

    # تنظيف الـ mask
    kernel = np.ones((5, 5), np.uint8)
    skin_mask = cv2.erode(skin_mask, kernel, iterations=1)
    skin_mask = cv2.dilate(skin_mask, kernel, iterations=2)
    skin_mask = cv2.morphologyEx(skin_mask, cv2.MORPH_OPEN, np.ones((5, 5), np.uint8))

    rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    results = pose.process(rgb)

    print(f"✓ Pose landmarks detected: {results.pose_landmarks is not None}")

    detected = False
    confidence = 0.0
    bounding_boxes = []

    if results.pose_landmarks:
        landmarks = results.pose_landmarks.landmark
        print(f"✓ Number of landmarks: {len(landmarks)}")

        exclude_mask = np.zeros((h, w), dtype=np.uint8)

        def pt(idx):
            return (int(landmarks[idx].x * w), int(landmarks[idx].y * h))

        face_points = np.array([[int(landmarks[i].x * w),
                                  int(landmarks[i].y * h)]
                                 for i in [0,1,2,3,4,5,6,7,8,9,10]],
                                dtype=np.int32)
        face_hull = cv2.convexHull(face_points)
        cv2.fillConvexPoly(exclude_mask, face_hull, 255)
        exclude_mask = cv2.dilate(exclude_mask, np.ones((35, 35), np.uint8), iterations=1)

        for idx in [15, 16, 27, 28, 31, 32]:
            x, y = pt(idx)
            cv2.circle(exclude_mask, (x, y), 70, 255, -1)

        body_points = np.array([[int(lm.x * w), int(lm.y * h)]
                                  for lm in landmarks], dtype=np.int32)
        body_mask = np.zeros((h, w), dtype=np.uint8)
        hull = cv2.convexHull(body_points)
        cv2.fillConvexPoly(body_mask, hull, 255)

        filtered_skin = cv2.bitwise_and(skin_mask, body_mask)
        filtered_skin = cv2.bitwise_and(filtered_skin, cv2.bitwise_not(exclude_mask))

        filtered_skin_area = cv2.countNonZero(filtered_skin)
        print(f"✓ Filtered skin pixels: {filtered_skin_area}")

        contours, _ = cv2.findContours(
            filtered_skin, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE
        )
        print(f"✓ Contours found: {len(contours)}")

        significant_contours = []
        for cnt in contours:
            area = cv2.contourArea(cnt)
            if area < 800:
                continue
            x, y, bw, bh = cv2.boundingRect(cnt)
            aspect_ratio = bw / float(bh)
            if aspect_ratio < 0.2 or aspect_ratio > 4:
                continue
            if x < 5 or y < 5:
                continue
            significant_contours.append((area, x, y, bw, bh))

        print(f"✓ Significant contours (after filters): {len(significant_contours)}")

        if significant_contours:
            skin_area = sum(a for a, *_ in significant_contours)
            total_area = h * w
            ratio = skin_area / total_area
            print(f"✓ Final skin ratio: {ratio:.3f} (threshold: {SKIN_RATIO_THRESHOLD})")

            if ratio > SKIN_RATIO_THRESHOLD:
                detected = True
                confidence = min(ratio * 2, 1.0)
                print(f"⚠ DETECTED! confidence={confidence:.2f}")

                for area, x, y, bw, bh in significant_contours:
                    bounding_boxes.append({
                        "x": x,
                        "y": y,
                        "width": bw,
                        "height": bh
                    })
            else:
                print(f"✗ Ratio {ratio:.3f} below threshold {SKIN_RATIO_THRESHOLD} — not detected")
        else:
            print("✗ No significant contours after filtering")

    else:
        print("✗ No pose landmarks — skipping skin analysis")

    action = "ALLOWED"
    if detected:
        action = "BLOCKED" if confidence > BLOCK_THRESHOLD else "FLAGGED"

    print(f"→ Final result: detected={detected}, action={action}, boxes={len(bounding_boxes)}")

    return {
        "detected": detected,
        "category": "ADULT" if detected else "NONE",
        "confidence": round(confidence, 2),
        "action": action,
        "contentType": "IMAGE",
        "boundingBoxes": bounding_boxes
    }