#!/bin/bash

echo "Stopping existing Docker services..."
docker-compose down

echo "Building all services with Maven..."
mvn clean package -DskipTests

echo "Rebuilding and starting Docker services with CORS fixes..."
docker-compose up -d --build

echo "CORS issues should now be fixed. Services are available at:"
echo "- Service Registry: http://localhost:8761"
echo "- API Gateway: http://localhost:8080"
echo "- Auth Service: http://localhost:8081"
echo "- Chat Service: http://localhost:8082"
echo "- HRMS Service: http://localhost:8083"
echo "- Frontend: http://localhost:3000" 