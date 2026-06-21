import torch
import cv2
from torchvision import models, transforms
from PIL import Image

device = "cuda" if torch.cuda.is_available() else "cpu"

# ===== Load model =====
model = models.mobilenet_v2(pretrained=False)
model.classifier[1] = torch.nn.Linear(1280, 2)
model.load_state_dict(torch.load("models/violence_model.pth", map_location=device))
model.to(device)
model.eval()

# ===== Transform =====
transform = transforms.Compose([
    transforms.Resize((224,224)),
    transforms.ToTensor(),
    transforms.Normalize([0.485,0.456,0.406],
                         [0.229,0.224,0.225])
])

# ===== Function to classify frame =====
def predict_frame(frame):
    rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    img = Image.fromarray(rgb)
    x = transform(img).unsqueeze(0).to(device)

    with torch.no_grad():
        out = model(x)
        prob = torch.softmax(out, dim=1)[0]
        label = torch.argmax(prob).item()

    if label == 1:
        return "Violence", prob[1].item()
    else:
        return "Non-Violence", prob[0].item()

# ===== Video IO =====
cap = cv2.VideoCapture("videos/input_video.mp4")

width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
fps = cap.get(cv2.CAP_PROP_FPS)

fourcc = cv2.VideoWriter_fourcc(*"mp4v")
out = cv2.VideoWriter(
    "output/output_violence.mp4",
    fourcc,
    fps,
    (width, height)
)

while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        break

    label, score = predict_frame(frame)

    color = (0,255,0)  # Non-Violence
    if label == "Violence":
        color = (0,0,255)

    cv2.putText(frame, f"{label} ({score:.2f})",
                (20,40),
                cv2.FONT_HERSHEY_SIMPLEX,
                1,
                color,
                2)

    out.write(frame)

cap.release()
out.release()
print("✅ Done! Check output/output_violence.mp4")
