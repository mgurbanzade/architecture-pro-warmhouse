FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/device-service.jar app.jar

ENV SERVER_PORT=8083
EXPOSE 8083

HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=6 \
  CMD wget -qO- http://127.0.0.1:8083/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
