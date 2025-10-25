# Hiring Platform - Microservices Backend

This repository contains the backend source code for a microservices-based Hiring Platform. The platform connects recruiters and job seekers, allowing recruiters to post jobs and candidates to search and apply for them.

## Features

- Microservices Architecture for scalability and maintainability
- Service Discovery using Netflix Eureka
- Centralized API Gateway with JWT-based security
- User Authentication and Authorization (Recruiters and Job Seekers)
- Job Posting and Search functionality
- Candidate Profile Management
- Email Notifications for job applications
- Inter-service communication using OpenFeign
- MongoDB for data persistence

## Tech Stack

**Language:** Java 21

**Framework:** Spring Boot 3, Spring Cloud

**Database:** MongoDB (MongoDB Atlas recommended)

**Service Discovery:** Netflix Eureka

**API Gateway:** Spring Cloud Gateway

**Inter-service Communication:** Spring Cloud OpenFeign

**Security:** JWT (via JJWT library)

**Email:** Spring Boot Mail Sender (Gmail SMTP)

**Build Tool:** Apache Maven## Architecture Overview

This project follows a microservices architecture to promote scalability, maintainability, and independent deployment. The key components are:

### Service Registry (Eureka Server)
Acts as a central directory where all microservices register themselves, enabling dynamic service discovery.

### API Gateway (Spring Cloud Gateway)
The single entry point for all external requests (e.g., from the frontend). It handles:
- Routing
- Security (JWT validation, authorization)
- Load balancing
- Response aggregation when needed

### Auth Service
Manages user registration, login, JWT generation, and provides user details.

### Job Service
Manages job postings (creation, retrieval, search) and handles the job application logic, including sending email notifications.

### Candidate Service
Manages detailed professional profiles for job seekers.

### Communication Flow
- Frontend interacts only with the API Gateway
- API Gateway routes requests to the appropriate microservice after validating security
- Microservices communicate with each other through the API Gateway or directly via Feign Clients using service discovery
- Example: Job Service fetches user details from Auth Service when needed## Environment Variables

To run this project, you will need to add the following environment variables:

### Required for Auth Service, Job Service, and Candidate Service:

`MONGO_DB_URI` - Your full MongoDB Atlas connection string (including username, password, cluster URL, and /hiring-platform database name)

Example: `mongodb+srv://<user>:<password>@<cluster-url>/hiring-platform?retryWrites=true&w=majority`

### Required for Auth Service and API Gateway:

`JWT_SECRET` - A strong, Base64 encoded secret key (at least 256 bits / 32+ original characters before encoding). Must be identical for both services.

Example (Generate your own!): `echo -n "YourVeryLongSecureSecretKey32CharsOrMore" | base64`

### Required for Job Service:

`GMAIL_USERNAME` - Your Gmail address used for sending emails

`GMAIL_APP_PASSWORD` - Your 16-character Gmail App Password

### Optional:

`EUREKA_URL` - The URL of your Eureka server (defaults to `http://localhost:8761/eureka/`)## Installation

### Prerequisites

- Java Development Kit (JDK) 21 or later
- Apache Maven
- MongoDB Instance (MongoDB Atlas recommended)
- A Gmail account with 2-Step Verification enabled and an App Password generated

### Clone the Repository

```bash
git clone <your-repository-url>
cd hiring-platform-microservices
```

### Build Services

Navigate into each service's directory and build it using Maven:

```bash
cd service-registry
mvn clean package

cd ../auth-service
mvn clean package

cd ../job-service
mvn clean package

cd ../candidate-service
mvn clean package

cd ../api-gateway
mvn clean package
```## Run Locally

The services must be started in the following order:

### 1. Start Service Registry

```bash
cd service-registry
java -jar target/service-registry-*.jar
```

Verify: Access http://localhost:8761 in your browser. You should see the Eureka dashboard.

### 2. Start Auth Service

```bash
cd ../auth-service
java -jar target/auth-service-*.jar
```

Verify: Check the Eureka dashboard; AUTH-SERVICE should appear as registered.

### 3. Start Job Service

```bash
cd ../job-service
java -jar target/job-service-*.jar
```

Verify: Check Eureka; JOB-SERVICE should appear.

### 4. Start Candidate Service

```bash
cd ../candidate-service
java -jar target/candidate-service-*.jar
```

Verify: Check Eureka; CANDIDATE-SERVICE should appear.

### 5. Start API Gateway

```bash
cd ../api-gateway
java -jar target/api-gateway-*.jar
```

Verify: Check Eureka; API-GATEWAY should appear.

The entire system is now running. All API requests should be directed to the API Gateway at http://localhost:8080.## API Reference

All API requests should be directed to the API Gateway at `http://localhost:8080`

### Authentication Endpoints (auth-service)

#### Register User

```
POST /register
```

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `username` | `string` | **Required**. Username |
| `password` | `string` | **Required**. Password |
| `role` | `string` | **Required**. RECRUITER or JOB_SEEKER |
| `email` | `string` | **Required**. Email address |

**Role:** Public

#### Login User

```
POST /login
```

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `username` | `string` | **Required**. Username |
| `password` | `string` | **Required**. Password |

**Role:** Public
**Returns:** JWT token

#### Get User Details

```
GET /users/{id}
```

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `id` | `string` | **Required**. User ID |

**Role:** Public (for inter-service use)

### Job Endpoints (job-service)

#### Get All Jobs

```
GET /posts/all
```

**Role:** Public

#### Search Jobs

```
GET /posts/search/{text}
```

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `text` | `string` | **Required**. Search keyword |

**Role:** Public

#### Get Job by ID

```
GET /posts/{id}
```

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `id` | `string` | **Required**. Job posting ID |

**Role:** Public

#### Create Job Posting

```
POST /posts/add
```

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `role` | `string` | **Required**. Job role/title |
| `description` | `string` | **Required**. Job description |
| `experience` | `number` | **Required**. Years of experience |
| `skillSet` | `array` | **Required**. Array of skills |
| `recruiterId` | `string` | **Required**. Recruiter user ID |

**Role:** RECRUITER (Requires valid Recruiter JWT)

#### Apply for Job

```
POST /posts/apply/{jobId}
```

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `jobId` | `string` | **Required**. Job posting ID |

**Role:** JOB_SEEKER (Requires valid Job Seeker JWT)
**Note:** Requires X-User-ID header (added by Gateway). Sends email to recruiter.

### Candidate Endpoints (candidate-service)

#### Create/Update Profile

```
POST /candidate/profile
```

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `fullName` | `string` | **Required**. Full name |
| `email` | `string` | **Required**. Email address |
| `totalExperience` | `number` | **Required**. Years of experience |
| `skills` | `array` | **Required**. Array of skills |
| `resumeUrl` | `string` | **Required**. Resume URL |

**Role:** JOB_SEEKER (Requires valid Job Seeker JWT)
**Note:** Requires X-User-ID header (added by Gateway)

#### Get Profile by User ID

```
GET /candidate/profile/{userId}
```

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `userId` | `string` | **Required**. User ID |

**Role:** Authenticated (Requires any valid JWT)

#### Get Profile for Inter-service Use

```
GET /candidate/profile/user/{userId}
```

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `userId` | `string` | **Required**. User ID |

**Role:** Public (for inter-service use)## Deployment

This repository contains the following microservice projects:

### Service Registry
- **Directory:** `service-registry/`
- **Port:** 8761
- **Description:** Eureka Server for service discovery

### API Gateway
- **Directory:** `api-gateway/`
- **Port:** 8080
- **Description:** Spring Cloud Gateway - single entry point for all external requests

### Auth Service
- **Directory:** `auth-service/`
- **Port:** 8081
- **Description:** Handles authentication and user data

### Job Service
- **Directory:** `job-service/`
- **Port:** 8082
- **Description:** Handles job postings and applications

### Candidate Service
- **Directory:** `candidate-service/`
- **Port:** 8083
- **Description:** Handles candidate profiles
