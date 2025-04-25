FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
COPY app.py .
COPY firebase-credentials.json .

ENV FLASK_APP=app.py
ENV FLASK_ENV=production

RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 6000

CMD ["python", "app.py"]