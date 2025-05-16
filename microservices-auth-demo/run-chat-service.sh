#!/bin/bash
echo "Starting Chat Service..."
cd chat-service
mvn spring-boot:run > chat-service.log 2>&1 &
echo "Chat Service started. Check chat-service.log for details." 