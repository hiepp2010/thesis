#!/bin/bash
echo "Starting Service Registry..."
cd service-registry
mvn spring-boot:run > service-registry.log 2>&1 &
echo "Service Registry started. Check service-registry.log for details." 