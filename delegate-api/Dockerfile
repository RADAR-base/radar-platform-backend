ARG VERSION=1.0.0
FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /app
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/
COPY gradlew .
RUN chmod +x /app/gradlew
COPY . .
RUN ./gradlew :delegate-api:shadowJar --no-daemon

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY --from=builder /app/delegate-api/build/libs/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"] 