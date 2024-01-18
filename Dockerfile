# Basic image:
FROM openjdk:8-jre-slim
# Author:
MAINTAINER ShucunZhao
# Configuration:
ENV PARAMS=""
# Define time zone var(PRC is China-time-zone):
ENV TZ=PRC
# Set the time zone in docker:
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# Add application file: Copy the file located at target/chatgpt-api.jar on the host to the root directory of the image and rename it to chatgpt-api.jar.
# This is typically an executable JAR file after building a Java application.
ADD target/chatgpt-api_v2-0.0.1-SNAPSHOT.jar /chatgpt-api.jar
## Commands executed after the image is run as a container
#ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /chatgpt-api.jar $PARAMS"]
ENTRYPOINT ["java","-jar","/chatgpt-api.jar"]