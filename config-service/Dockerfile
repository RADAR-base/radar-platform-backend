FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /app

COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/
COPY gradlew .
RUN chmod +x /app/gradlew
COPY . .

RUN ./gradlew :config-service:shadowJar --no-daemon

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /app/config-service/build/libs/*-all.jar /app/app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]


