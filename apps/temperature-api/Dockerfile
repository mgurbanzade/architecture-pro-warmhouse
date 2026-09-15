FROM python:3.12-alpine AS base
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY app.py .

FROM base AS test
COPY test_app.py .
RUN pip install --no-cache-dir pytest && python -m pytest -q test_app.py && touch /tests-passed

FROM base AS runtime
COPY --from=test /tests-passed /tests-passed
ENV PORT=8081
EXPOSE 8081
HEALTHCHECK --interval=5s --timeout=3s --retries=5 \
  CMD wget -qO- http://127.0.0.1:8081/health || exit 1
CMD ["python", "app.py"]
