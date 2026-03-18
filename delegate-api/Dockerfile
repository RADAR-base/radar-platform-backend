FROM --platform=$BUILDPLATFORM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY core/build.gradle.kts core/
COPY contract/build.gradle.kts contract/
COPY delegate-api/build.gradle.kts delegate-api/

RUN chmod +x gradlew && ./gradlew --no-daemon \
    :delegate-api:downloadDependencies \
    :delegate-api:copyDependencies \
    :delegate-api:startScripts

COPY core/src/ core/src/
COPY contract/src/ contract/src/
COPY delegate-api/src/ delegate-api/src/

RUN ./gradlew --no-daemon :delegate-api:jar

FROM eclipse-temurin:17-jre-jammy

COPY --from=builder /app/delegate-api/build/scripts/* /usr/bin/
COPY --from=builder /app/delegate-api/build/third-party/* /usr/lib/
COPY --from=builder /app/delegate-api/build/libs/*.jar /usr/lib/
COPY --from=builder /app/delegate-api/src/main/resources/config.yaml /etc/delegate-api/config.yaml

USER 101

EXPOSE 8080

ENTRYPOINT ["delegate-api"]
