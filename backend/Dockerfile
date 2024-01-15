FROM maven:latest AS maven
LABEL MAINTAINER=""

WORKDIR /usr/src/app
COPY . /usr/src/app
# Compile and package the application to an executable JAR
RUN mvn package 
RUN ls -la /usr/src/app/target

# For Java 21
FROM sapmachine:latest

ARG JAR_FILE=backend-1.0.0-SNAPSHOT.jar

WORKDIR /opt/app

COPY --from=maven /usr/src/app/target/${JAR_FILE} /opt/app/

# Expose port 8080 to the host
EXPOSE 8080

# Run the web service on container startup.
ENTRYPOINT ["java","-jar","backend-0.0.1-SNAPSHOT.jar"]