FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache ffmpeg

COPY ./rinko-oss/target/rinko-oss-1.0.0-SNAPSHOT.jar app.jar