FROM --platform=$BUILDPLATFORM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY core/build.gradle.kts core/
COPY config-service/build.gradle.kts config-service/

RUN chmod +x gradlew && ./gradlew --no-daemon \
    :config-service:downloadDependencies \
    :config-service:copyDependencies \
    :config-service:startScripts

COPY core/src/ core/src/
COPY config-service/src/ config-service/src/

RUN ./gradlew --no-daemon :config-service:jar

FROM eclipse-temurin:17-jre-jammy

COPY --from=builder /app/config-service/build/scripts/* /usr/bin/
COPY --from=builder /app/config-service/build/third-party/* /usr/lib/
COPY --from=builder /app/config-service/build/libs/*.jar /usr/lib/
COPY --from=builder /app/config-service/src/main/resources/config.yaml /etc/config-service/config.yaml

USER 101

EXPOSE 8083

ENTRYPOINT ["config-service"]
