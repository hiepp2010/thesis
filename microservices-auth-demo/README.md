# Microservices Authorization Demo

This project demonstrates a microservices architecture with API Gateway-based JWT authorization. It consists of the following components:

## Components

1. **Service Registry (Eureka Server)**
   - Service discovery for all microservices
   - Port: 8761

2. **API Gateway**
   - Entry point for all client requests
   - JWT token validation
   - Request routing to appropriate services
   - Port: 8080

3. **Auth Service**
   - User registration and authentication
   - JWT token generation
   - Port: 8081

4. **Chat Service**
   - Chat functionality
   - Protected by JWT authentication
   - Port: 8082

5. **HRMS Service**
   - HR management functionality
   - Protected by JWT authentication
   - Port: 8083

6. **Frontend Application**
   - Next.js application for testing the microservices
   - User registration and login
   - Dashboard for testing protected endpoints
   - Port: 3000

## Architecture

```
Client → API Gateway → [Auth Service, Chat Service, HRMS Service] ↔ Service Registry
```

## Security Flow

1. User registers or logs in through the Auth Service
2. Auth Service generates a JWT with user roles and permissions
3. Client uses this JWT for all subsequent requests to the API Gateway
4. API Gateway validates the JWT and forwards the request to the appropriate service
5. Each service uses the JWT claims for fine-grained authorization

## Running the Application

### Using Docker (Recommended)

The easiest way to run the application is with Docker:

```bash
cd microservices-auth-demo
./docker-build-run.sh
```

This will:
1. Build all services with Maven
2. Build Docker images for all services
3. Start all the services with docker-compose

To stop all services:

```bash
./docker-stop.sh
```

### Manual Method (Without Docker)

You can start all backend services with the provided script:

```bash
cd microservices-auth-demo
./start-services.sh
```

Or manually start each service:

1. Start the Service Registry:
   ```
   cd service-registry
   mvn spring-boot:run
   ```

2. Start the Auth Service:
   ```
   cd auth-service
   mvn spring-boot:run
   ```

3. Start the Chat Service:
   ```
   cd chat-service
   mvn spring-boot:run
   ```

4. Start the HRMS Service:
   ```
   cd hrms-service
   mvn spring-boot:run
   ```

5. Start the API Gateway:
   ```
   cd api-gateway
   mvn spring-boot:run
   ```

### Frontend Application

When running manually, start the frontend with:

```bash
cd frontend
npm install
npm run dev
```

Then access the application at http://localhost:3000

### Stopping Services

To stop all backend services when running manually:

```bash
./stop-services.sh
```

## API Endpoints

### Auth Service

- POST `/api/auth/register` - Register a new user
- POST `/api/auth/authenticate` - Login and receive JWT
- GET `/api/auth/check-username/{username}` - Check if username exists
- GET `/api/auth/check-email/{email}` - Check if email exists

### Chat Service

- GET `/api/chat/public/status` - Public endpoint
- GET `/api/chat/private/status` - Protected endpoint

### HRMS Service

- GET `/api/hrms/public/status` - Public endpoint
- GET `/api/hrms/private/status` - Protected endpoint
- GET `/api/hrms/private/employees` - Protected endpoint

## Testing the Application

1. Register a new user through the frontend or by sending a POST request to `/api/auth/register`
2. Login with the registered credentials
3. Use the received JWT token in the `Authorization` header for protected endpoints
4. Explore the dashboard to see responses from different microservices 