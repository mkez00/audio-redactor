Audio Redactor
=

Overview
-

Audio Redactor is an application which exposes a JSON-RPC web service (HTTP) for users that need to redact sections of a provided audio file.  The application requires FFMPEG to do the dirty work.

Usage
-

The easiest way to get up and running to test this application is to use Docker.  See Docker Image section below for instructions.

To redact segments of audio from an audio file, the following HTTP request is required:

URL: `POST http://localhost:8080/`

Headers: `Content-Type: application/json`

Request Payload:

```
{
   "payload": "<BASE64_REPRESENTATION_OF_AUDIO_FILE>",
   "segments": [{"startSecond":1, "endSecond":3},{"startSecond":8, "endSecond":10}],
   "sha256": "bBpFu/kkqaxq/YpXszHEzh6epQJdjS1RFMSgY5RcfB8=",
   "destinationFileName": "output.mp3"
}
```

`payload` : Base64 encoded representation of audio file to be redacted.  Currently only `.mp3` formats are functioning

`segments` : List of start and end seconds to be redacted.  In the example above audio between 1-3 seconds and 8-10 will be redacted

`sha256` (optional) : Hash (SHA256) of the incoming file.  The service will fail if the hash does not match the incoming payload.  If no hash is provided the check will not occur

`destinationFileName` (optional) : Override the filename of the processed file (to be used when the application is configured to write to a custom location and file deletion is disabled)

Response Payload

```
{
    "payload": "<BASE64_REPRESENTATION_OF_REDACTED_AUDIO>",
    "sha256": "q2pFu/poqaxq/XpXszYGzh6epHJdYS1RFMSgB5PcpI9="
}
```

`payload` : Base64 encoded representation of the modified audio file with redactions applied

`sha256` : SHA256 hash of the redacted audio file

Requirements and Install
-

In order to build and run the project locally the following resources are required:

- FFMPEG is required on the host machine
- Java 8
- Maven

After the tools above have been installed and configured, follow these steps to run the application locally

1. Clone/download repository
2. From project root.  Run `mvn clean package`
3. Run the application `java -jar target/audio-redactor-0.0.1-SNAPSHOT.jar`

NOTE: To run the application with custom application parameters (for example persist the converted file to another directory), provide them as follows: `java -Ddelete.temp.destination.media=false -Ddestination.media.file.location=/var/log/audio-redactor/ -jar target/audio-redactor-0.0.1-SNAPSHOT.jar`

Docker Image
-

A Docker image is available for use which packages the application and FFMPEG into a single Docker image.

To download Docker image and run:

1. Pull Docker image: `docker pull mkez00/audio-redactor`
2. Create and run Docker container: `docker run -p 8080:8080 mkez00/audio-redactor:latest`
3. Start using audio-redactor

To build your own Docker image:

1. Clone/download repository
2. From project root.  Run `mvn clean package`
3. Build docker image `docker build . -t YOUR_REPO/audio-redactor`
4. Push to docker repo `docker push YOUR_REPO/audio-redactor`

Environment variables (map to Application Parameters in next section):

`DELETE_SOURCE_FILE`: (default = true) When the service accepts the audio file from the client, the application deletes the file after it completes the transaction (unless set to false)

`DELETE_DESTINATION_FILE`: (default = true) When the service converts the file it creates a new file on the file system which is the modified file.  After returning the response to the user this file is deleted (set to false if you want the file to not be deleted)

`DESTINATION_DIRECTORY`: (default = /var/audio-redactor/) The output directory for the redacted audio file

Example usage: `docker run -e DELETE_DESTINATION_FILE=false -p 8888:8080 mkez00/audio-redactor:latest`

These environment variables were made available to allow the user to potentially use a `volumeMount` (Kubernetes) or equivalent (like a bind mount in Docker) to persist the file outside of the Docker container after processing instead of the client managing the returned payload.

Application Parameters
-

`delete.temp.source.media` (default = true) : After the service decodes the audio file from the client, should the application remove the source file from the file system

`delete.temp.destination.media` (default = true) : The service creates a new file on the hosts file system with the redacted audio, should the application remove this file from the file system after the response is returned to the client

`destination.media.file.location` (default = null) : Override the destination directory of the redacted audio file.  Default uses Java temp storage on file system

