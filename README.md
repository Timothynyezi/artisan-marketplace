
Artisan Marketplace Platform
A backend-focused Java/Spring Boot application connecting clients seeking home renovation services with freelance artisans (plumbers, electricians, welders, painters, builders).

This project is built as a collaborative design and implementation exercise following the Software Development Lifecycle (SDLC), prioritizing correctness of the core job lifecycle, trust signals, and robust state management.

🚧 Project Status: Phase 1 (MVP) - In Progress
The core domain logic, authentication, and state machine are complete. The project currently includes full test coverage for the implemented vertical slices.

Completed Slices:

User Auth & JWT: Stateless authentication with access/refresh token rotation logic, BCrypt hashing, and role-based access control.

Artisan Profiles: CRUD operations for artisan bios, rates, and locations.

Trade Lookup: Seeded lookup table for supported trades (Plumber, Electrician, etc.) and many-to-many mapping to artisans.

Job State Machine: The core domain logic. Enforces strict, guarded state transitions (e.g., OPEN → REQUESTED → ACCEPTED) and prevents illegal skips (e.g., OPEN directly to COMPLETED).

Upcoming Slices (Next Steps):

□ Job Responses: Allow artisans to submit proposals on OPEN_POST jobs.
□ Location Matching (PostGIS): Spatial indexing to match jobs with nearby artisans based on distance and Bayesian rating sorting.
□ Portfolio & Qualifications: Media uploads and verified trade evidence.
□ Reviews: Client rating system that feeds back into the ranking algorithm.
□ Payments: Paystack integration with webhook idempotency for secure checkout.
🛠️ Technology Stack
Language: Java 25

Framework: Spring Boot 4.1.0

Persistence: Spring Data JPA, Hibernate

Database: PostgreSQL 14+ (PostGIS integration planned)

Security: Spring Security + JJWT (Stateless JWT)

Build Tool: Maven

🚀 How to Run and Test the Project
Prerequisites
Java 25 installed (or newer).

PostgreSQL installed and running.

Maven (the project includes the mvnw wrapper, so standalone Maven is optional).

1. Database Setup
Create a local PostgreSQL user and database. Do not use your production credentials.

sql
-- Run this in your psql terminal
CREATE USER <YOUR_LOCAL_DB_USER> WITH PASSWORD '<YOUR_LOCAL_SECURE_PASSWORD>';
CREATE DATABASE artisan_marketplace OWNER <YOUR_LOCAL_DB_USER>;
2. Environment Configuration
The application uses Spring Profiles to keep secrets out of version control.
The file application-local.yml is intentionally git-ignored and must be created locally.

Create src/main/resources/application-local.yml.

Add your local database credentials and a secure JWT secret:

yaml
spring:
  datasource:
    username: ${DB_USERNAME:<YOUR_LOCAL_DB_USER>}
    password: ${DB_PASSWORD:<YOUR_LOCAL_SECURE_PASSWORD>}

jwt:
  # Generate a secure 32+ byte secret using: openssl rand -base64 48
  secret: ${JWT_SECRET:<YOUR_GENERATED_SECRET>}
  access-token-expiration-ms: 900000
  refresh-token-expiration-ms: 604800000
(Note: The ${VARIABLE:default} syntax allows you to override these values using environment variables in CI/CD pipelines or production without changing the file.)

3. Build and Test
Run the full suite of unit, slice, and integration tests:

bash
./mvnw clean test
(You should see all 41+ passing tests, covering the state machine, services, security configurations, and controller endpoints).

4. Boot the Application
Start the server with the local profile active:

bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
The app will start on http://localhost:8080. On first boot, a DataInitializer will automatically seed the trades table.

5. Manual API Testing (cURL)
To test the API manually, you can use the following sequence. Ensure you replace all <...> placeholders with your actual test data.

Step A: Register a Client

bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client@example.com",
    "password": "<YOUR_TEST_PASSWORD>",
    "role": "CLIENT",
    "fullName": "Test Client",
    "phone": "0821234567"
  }'
Step B: Login and capture the Token
Note: This command uses jq to extract the token into a variable. If you don't have jq installed, you can copy the accessToken value manually from the JSON response.

bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"client@example.com","password":"<YOUR_TEST_PASSWORD>"}' \
  | jq -r '.accessToken')

# Verify the token was captured (prints the JWT)
echo $TOKEN
Step C: Create a Job

bash
curl -X POST http://localhost:8080/api/v1/jobs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "tradeRequired": "PLUMBER",
    "title": "Fix leaking sink",
    "description": "The pipe under the kitchen sink is leaking.",
    "latitude": -33.9249,
    "longitude": 18.4241,
    "locationName": "Cape Town CBD",
    "origin": "OPEN_POST",
    "budget": 1500.0
  }'
(Copy the id from the JSON response to use in the next step).

Step D: Transition Job Status

bash
# Replace <YOUR_JOB_ID> with the actual UUID from Step C
curl -X PATCH "http://localhost:8080/api/v1/jobs/<YOUR_JOB_ID>/status?targetStatus=CANCELLED" \
  -H "Authorization: Bearer $TOKEN"
🏗️ Architecture Notes
Package-by-Feature: Code is organized by domain (e.g., job, artisan, user) rather than technical layer to ensure high cohesion.

State Pattern (Guard Clause): JobStatus uses a centralized transition map rather than a full Gang-of-Four State pattern, striking a balance between strict correctness and lightweight maintainability.

Composition over Inheritance: User and ArtisanProfile are separate entities sharing a 1-to-1 relationship, avoiding JPA inheritance complexity.

Stateless Security: No server-side sessions. The application relies entirely on short-lived JWTs and explicit resource-level ownership checks in the service layer.

License
This project is for portfolio purposes.
