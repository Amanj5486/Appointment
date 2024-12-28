FROM --platform=linux/amd64 amazoncorretto:17.0.12-al2
VOLUME /tmp
COPY target/appointmentlambda.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]