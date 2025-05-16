#!/bin/bash
echo "Starting API Gateway..."
cd api-gateway
mvn spring-boot:run > api-gateway.log 2>&1 &
echo "API Gateway started. Check api-gateway.log for details." 