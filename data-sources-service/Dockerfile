FROM --platform=$BUILDPLATFORM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY core/build.gradle.kts core/
COPY data-sources-service/build.gradle.kts data-sources-service/

RUN chmod +x gradlew && ./gradlew --no-daemon \
    :data-sources-service:downloadDependencies \
    :data-sources-service:copyDependencies \
    :data-sources-service:startScripts

COPY core/src/ core/src/
COPY data-sources-service/src/ data-sources-service/src/

RUN ./gradlew --no-daemon :data-sources-service:jar

FROM eclipse-temurin:17-jre-jammy

COPY --from=builder /app/data-sources-service/build/scripts/* /usr/bin/
COPY --from=builder /app/data-sources-service/build/third-party/* /usr/lib/
COPY --from=builder /app/data-sources-service/build/libs/*.jar /usr/lib/
COPY --from=builder /app/data-sources-service/src/main/resources/config.yaml /etc/data-sources-service/config.yaml

USER 101

EXPOSE 8084

ENTRYPOINT ["data-sources-service"]
