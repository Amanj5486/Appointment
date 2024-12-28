FROM amazoncorretto:17.0.12-al2

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/appointmentlambda.jar app.jar

# Set a volume for temporary files
VOLUME /tmp

# Specify the command to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]



#FROM --platform=linux/amd64 amazoncorretto:17.0.12-al2
#VOLUME /tmp
#COPY target/appointmentlambda.jar app.jar
#ENTRYPOINT ["java","-jar","/app.jar"]