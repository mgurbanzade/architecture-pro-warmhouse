FROM python:3.12-slim AS base
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY app ./app

FROM base AS test
COPY tests ./tests
RUN pip install --no-cache-dir pytest && python -m pytest -q tests && touch /tests-passed

FROM base AS runtime
COPY --from=test /tests-passed /tests-passed
ENV PORT=8082
EXPOSE 8082
HEALTHCHECK --interval=5s --timeout=3s --start-period=10s --retries=6 \
  CMD python -c "import urllib.request,sys; sys.exit(0 if urllib.request.urlopen('http://127.0.0.1:8082/health').status == 200 else 1)"
CMD ["python", "-m", "app.main"]
