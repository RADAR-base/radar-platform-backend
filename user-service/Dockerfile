FROM --platform=$BUILDPLATFORM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY core/build.gradle.kts core/
COPY user-service/build.gradle.kts user-service/

RUN chmod +x gradlew && ./gradlew --no-daemon \
    :user-service:downloadDependencies \
    :user-service:copyDependencies \
    :user-service:startScripts

COPY core/src/ core/src/
COPY user-service/src/ user-service/src/

RUN ./gradlew --no-daemon :user-service:jar

FROM eclipse-temurin:17-jre-jammy

COPY --from=builder /app/user-service/build/scripts/* /usr/bin/
COPY --from=builder /app/user-service/build/third-party/* /usr/lib/
COPY --from=builder /app/user-service/build/libs/*.jar /usr/lib/
COPY --from=builder /app/user-service/src/main/resources/config.yaml /etc/user-service/config.yaml

USER 101

EXPOSE 8081

ENTRYPOINT ["user-service"]
