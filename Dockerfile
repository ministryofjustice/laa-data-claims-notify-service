# Specify java runtime base image
FROM amazoncorretto:25-alpine

# Set up working directory in the container
RUN mkdir -p /opt/laa-data-claims-notify-service/
WORKDIR /opt/laa-data-claims-notify-service/

# Copy the JAR file into the container
COPY laa-data-claims-notify-service/build/libs/laa-data-claims-notify-service-0.0.0-SNAPSHOT.jar app.jar

# Create a group and non-root user
RUN addgroup -S appgroup && adduser -u 1001 -S appuser -G appgroup

# Set the default user
USER 1001

# Expose the management.server.port for the application
EXPOSE 8183

# Run the JAR file
CMD java -jar app.jar