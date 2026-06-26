FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY chatbot_main.py .
COPY GuardianAI_Features.md .

EXPOSE 8000

CMD ["uvicorn", "chatbot_main:app", "--host", "0.0.0.0", "--port", "8000"]
