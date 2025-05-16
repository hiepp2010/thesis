#!/bin/bash
echo "Starting Auth Service..."
cd auth-service
mvn spring-boot:run > auth-service.log 2>&1 &
echo "Auth Service started. Check auth-service.log for details." 