# Multi-stage build for Spring Boot application
FROM gradle:8.8-jdk21 AS build
WORKDIR /workspace
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN ./gradlew --version >/dev/null
COPY . .
RUN ./gradlew bootJar --no-daemon

FROM curlimages/curl:8.8.0 AS otelagent
ARG OTEL_AGENT_VERSION=2.13.3
RUN curl -fsSL -o /tmp/opentelemetry-javaagent.jar \
  "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar"

FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR_FILE=build/libs/*.jar
COPY --from=build /workspace/${JAR_FILE} app.jar
COPY --from=otelagent /tmp/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
