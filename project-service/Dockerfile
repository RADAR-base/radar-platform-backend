FROM --platform=$BUILDPLATFORM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY core/build.gradle.kts core/
COPY project-service/build.gradle.kts project-service/

RUN chmod +x gradlew && ./gradlew --no-daemon \
    :project-service:downloadDependencies \
    :project-service:copyDependencies \
    :project-service:startScripts

COPY core/src/ core/src/
COPY project-service/src/ project-service/src/

RUN ./gradlew --no-daemon :project-service:jar

FROM eclipse-temurin:17-jre-jammy

COPY --from=builder /app/project-service/build/scripts/* /usr/bin/
COPY --from=builder /app/project-service/build/third-party/* /usr/lib/
COPY --from=builder /app/project-service/build/libs/*.jar /usr/lib/
COPY --from=builder /app/project-service/src/main/resources/config.yaml /etc/project-service/config.yaml

USER 101

EXPOSE 8082

ENTRYPOINT ["project-service"]
