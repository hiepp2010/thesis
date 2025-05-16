#!/bin/bash
echo "Starting HRMS Service..."
cd hrms-service
mvn spring-boot:run > hrms-service.log 2>&1 &
echo "HRMS Service started. Check hrms-service.log for details." 