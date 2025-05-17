#!/bin/bash

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;36m'
NC='\033[0m' # No Color

# Define the URLs
GATEWAY_URL="http://localhost:8080"
AUTH_URL="${GATEWAY_URL}/api/auth/authenticate"
CHAT_HEALTH_URL="${GATEWAY_URL}/api/chat/public/health"
CHAT_PUBLIC_TEST_URL="${GATEWAY_URL}/api/chat/public/test"
CHAT_HELLO_URL="${GATEWAY_URL}/api/chat/hello"
CHAT_SERVICE_URL="http://localhost:8084"
CHAT_SERVICE_HELLO="${CHAT_SERVICE_URL}/api/chat/hello"

# Test credentials
USERNAME="linh"
PASSWORD="1234"

echo -e "${BLUE}Testing Chat Service API via Gateway and Direct Connection${NC}"

# Test 1: Check if the API Gateway is accessible
echo -e "\n${YELLOW}Test 1: Checking if API Gateway is accessible${NC}"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${GATEWAY_URL}")
echo "HTTP Status Code: ${HTTP_CODE}"
if [[ "${HTTP_CODE}" == "200" || "${HTTP_CODE}" == "302" || "${HTTP_CODE}" == "401" ]]; then
  echo -e "${GREEN}✓ API Gateway is accessible (returned ${HTTP_CODE})${NC}"
else
  echo -e "${RED}✗ API Gateway is not accessible (returned ${HTTP_CODE})${NC}"
  exit 1
fi

# Test 2: Test public health endpoint
echo -e "\n${YELLOW}Test 2: Testing public health endpoint${NC}"
HEALTH_RESPONSE=$(curl -s "${CHAT_HEALTH_URL}")
echo "Response: ${HEALTH_RESPONSE}"
if [[ "${HEALTH_RESPONSE}" == *"Chat service is running"* ]]; then
  echo -e "${GREEN}✓ Public health endpoint is working${NC}"
else
  echo -e "${RED}✗ Public health endpoint failed${NC}"
fi

# Test 3: Test public test endpoint
echo -e "\n${YELLOW}Test 3: Testing public test endpoint${NC}"
TEST_RESPONSE=$(curl -s "${CHAT_PUBLIC_TEST_URL}")
echo "Response: ${TEST_RESPONSE}"
if [[ "${TEST_RESPONSE}" == *"This is a public test endpoint"* ]]; then
  echo -e "${GREEN}✓ Public test endpoint is working${NC}"
else
  echo -e "${RED}✗ Public test endpoint failed${NC}"
fi

# Test 4: Get JWT token from auth service
echo -e "\n${YELLOW}Test 4: Getting JWT token from auth service${NC}"
AUTH_RESPONSE=$(curl -s -X POST "${AUTH_URL}" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}")

echo "Auth Response: ${AUTH_RESPONSE}"
TOKEN=$(echo "${AUTH_RESPONSE}" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "${TOKEN}" ]; then
  echo -e "${GREEN}✓ Successfully obtained JWT token${NC}"
else
  echo -e "${RED}✗ Failed to obtain JWT token${NC}"
  exit 1
fi

# Test 5: Access protected endpoint with JWT through gateway
echo -e "\n${YELLOW}Test 5: Testing protected hello endpoint with JWT through gateway${NC}"
GATEWAY_RESPONSE=$(curl -s "${CHAT_HELLO_URL}" \
  -H "Authorization: Bearer ${TOKEN}")

echo "Gateway Response: ${GATEWAY_RESPONSE}"
if [[ "${GATEWAY_RESPONSE}" == *"Hello from Chat Service"* ]]; then
  echo -e "${GREEN}✓ Protected endpoint is working with JWT through gateway${NC}"
else
  echo -e "${RED}✗ Protected endpoint failed with JWT through gateway${NC}"
  GATEWAY_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${CHAT_HELLO_URL}" \
    -H "Authorization: Bearer ${TOKEN}")
  echo "  Gateway HTTP Status: ${GATEWAY_STATUS}"
fi

# Test 6: Access protected endpoint with JWT directly
echo -e "\n${YELLOW}Test 6: Testing protected hello endpoint with JWT directly${NC}"
DIRECT_RESPONSE=$(curl -s "${CHAT_SERVICE_HELLO}" \
  -H "Authorization: Bearer ${TOKEN}")

echo "Direct Response: ${DIRECT_RESPONSE}"
if [[ "${DIRECT_RESPONSE}" == *"Hello from Chat Service"* ]]; then
  echo -e "${GREEN}✓ Protected endpoint is working with JWT directly${NC}"
else
  echo -e "${RED}✗ Protected endpoint failed with JWT directly${NC}"
  DIRECT_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${CHAT_SERVICE_HELLO}" \
    -H "Authorization: Bearer ${TOKEN}")
  echo "  Direct HTTP Status: ${DIRECT_STATUS}"
fi

echo -e "\n${BLUE}Testing completed${NC}" 