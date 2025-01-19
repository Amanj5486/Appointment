FROM --platform=linux/amd64 amazoncorretto:17.0.12-al2
VOLUME /tmp
COPY target/functions-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]