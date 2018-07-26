FROM openjdk:8-jdk-alpine

USER root

RUN apk update
RUN apk add ffmpeg
RUN mkdir /var/audio-redactor

ADD target/audio-redactor-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080

ENV DELETE_SOURCE_FILE="true"
ENV DELETE_DESTINATION_FILE="true"
ENV DESTINATION_DIRECTORY="/var/audio-redactor/"

ENTRYPOINT exec java -Ddelete.temp.source.media=$DELETE_SOURCE_FILE -Ddelete.temp.destination.media=$DELETE_DESTINATION_FILE -Ddestination.media.file.location=$DESTINATION_DIRECTORY -Djava.security.egd=file:/dev/./urandom -jar /app.jar