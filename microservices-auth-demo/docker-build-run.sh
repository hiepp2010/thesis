#!/bin/bash

echo "Building all services with Maven first..."
./mvnw clean package -DskipTests

echo "Building Docker images and starting all services..."
docker-compose up -d --build

echo "Services should be available at:"
echo "- Service Registry: http://localhost:8761"
echo "- API Gateway: http://localhost:8080"
echo "- Auth Service: http://localhost:8081"
echo "- Chat Service: http://localhost:8082"
echo "- HRMS Service: http://localhost:8083"
echo "- Frontend: http://localhost:3000"
echo ""
echo "To stop all services run: docker-compose down" 