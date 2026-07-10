FROM --platform=$BUILDPLATFORM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY core/build.gradle.kts core/
COPY gateway-service/build.gradle.kts gateway-service/

RUN chmod +x gradlew && ./gradlew --no-daemon \
    :gateway-service:downloadDependencies \
    :gateway-service:copyDependencies \
    :gateway-service:startScripts

COPY core/src/ core/src/
COPY gateway-service/src/ gateway-service/src/

RUN ./gradlew --no-daemon :gateway-service:jar

FROM eclipse-temurin:17-jre-jammy

COPY --from=builder /app/gateway-service/build/scripts/* /usr/bin/
COPY --from=builder /app/gateway-service/build/third-party/* /usr/lib/
COPY --from=builder /app/gateway-service/build/libs/*.jar /usr/lib/
COPY --from=builder /app/gateway-service/src/main/resources/config.yaml /etc/gateway-service/config.yaml

USER 101

EXPOSE 8085

ENTRYPOINT ["gateway-service"]
