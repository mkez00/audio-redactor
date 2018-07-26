FROM openjdk:8-jdk-alpine

USER root

RUN apk update
RUN apk add ffmpeg

ADD target/audio-redactor-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080

ENTRYPOINT exec java -Djava.security.egd=file:/dev/./urandom -jar /app.jar