#!/bin/bash

# Build the application
echo "Building the application..."
mvn clean package -DskipTests

# Check if the build was successful
if [ $? -ne 0 ]; then
  echo "Build failed"
  exit 1
fi

# Run with MySQL connection
echo "Starting the application with MySQL connection..."
java -jar target/chat-service-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:mysql://localhost:3307/authdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=rootpassword 