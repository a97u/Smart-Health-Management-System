# Smart Health Management System

A comprehensive hospital management platform built with Spring Boot that facilitates patient management, appointment scheduling, medical record handling, and document management through a secure REST API.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [System Requirements](#system-requirements)
- [Dependencies](#dependencies)
- [Installation Guide](#installation-guide)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Testing with Postman](#testing-with-postman)
- [License](#license)
- [Future Enhancements](#future-enhancements)

## Overview

The Smart Health Management System is designed to digitize and streamline hospital and clinic operations. It helps manage patient records, appointments, user roles (admin, doctors, nurses, and patients), and medical documents efficiently and securely.

## Features

### User Management
- Role-based access: Admin, Doctor, Nurse, Patient
- Secure authentication using Spring Security

### Patient Management
- Patient registration and profile management
- Tracking of health metrics and medical history
- Document upload and management

### Doctor Management
- Doctor registration and profile editing
- Specialization and availability tracking
- Medical record access and management

### Appointment System
- Scheduling, rescheduling, and cancelling appointments
- Tracking appointment status
- Automated notification support

### Medical Records
- Medical record creation and updates
- Secure association with patients and doctors
- Restricted access control

### Document Management
- Uploading and storing patient documents
- Categorizing documents
- Secure access and downloading features

## System Requirements

- Java JDK 17 or higher
- MySQL 8.0 or higher
- Maven 3.8.0 or higher
- Supported Operating Systems: Windows, Linux, or macOS

## Dependencies

- Spring Boot 3.0.x
- Spring Security
- Spring Data JPA
- Lombok
- MySQL Connector
- JFreeChart (for health metrics visualization)
- Jakarta Validation
- SLF4J and Logback for logging

## Installation Guide

### Prerequisites

Install the following tools before setting up the application:
- JDK 17 or higher
- Maven 3.8.0 or higher
- MySQL 8.0 or higher

### Step 1: Clone the Repository

```bash
git clone https://github.com/a97u/Smart-Health-Management-System.git
cd Smart-Health-Management-System
```

### Step 2: Configure Database

1. Create the MySQL database:
```sql
CREATE DATABASE hospital;
```

2. Update the `application.properties` file:
```properties
spring.application.name=HealthCare
spring.profiles.active=email
spring.datasource.url=jdbc:mysql://localhost:3306/hospital
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
```

### Step 3: Build the Application

```bash
mvn clean install
```

### Step 4: Run the Application

```bash
mvn spring-boot:run
```

The application will be available at http://localhost:8080

## Configuration

### Database Configuration

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hospital
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### JPA Configuration

```properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Logging Configuration

```properties
logging.level.com.hospital=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate=DEBUG
logging.file.name=logs/healthcare.log
```

### File Upload Configuration

```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.file-size-threshold=2KB
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=15MB
```

### Email Configuration

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## API Documentation

### Authentication APIs

| Method | Endpoint                        | Description                      | Role         |
|--------|----------------------------------|----------------------------------|--------------|
| GET    | /api/home                       | Home page                         | Public       |
| GET    | /api/dashboard                   | Redirect to role-specific dashboard | Authenticated |
| POST   | /api/auth/register/doctor        | Register a new doctor             | Public       |
| POST   | /api/auth/register/nurse         | Register a new nurse              | Public       |
| POST   | /api/auth/register/patient       | Register a new patient            | Public       |

### Admin APIs

| Method | Endpoint                | Description       | Role  |
|--------|-----------------------------|-------------------|-------|
| GET    | /api/admin/dashboard       | Admin dashboard    | Admin |
| GET    | /api/admin/statistics      | System statistics  | Admin |

### User Management APIs

| Method | Endpoint             | Description      | Role          |
|--------|------------------------|------------------|---------------|
| GET    | /api/users/profile     | Get user profile  | Authenticated |
| GET    | /api/users             | Get all users     | Admin         |
| GET    | /api/users/{id}        | Get user by ID    | Admin         |
| PUT    | /api/users/{id}        | Update user       | Admin         |
| DELETE | /api/users/{id}        | Delete user       | Admin         |

For a complete list of APIs, please refer to the [API Documentation](https://github.com/a97u/Smart-Health-Management-System/wiki/API-Documentation).

## Database Schema

The system uses the following database tables:

1. **user** - Stores core user credentials and identity
2. **role** - Defines the roles that users can have
3. **userRole** - Junction table for many-to-many relationship between users and roles
4. **patient** - Stores detailed info for patients
5. **doctor** - Stores detailed info for doctors
6. **nurse** - Stores detailed info for nurses
7. **record** - Stores medical visit records
8. **patient_health_metrics** - Stores health metrics for patients
9. **document** - Stores uploaded medical documents
10. **appointment** - Stores doctor-patient appointments

For detailed schema information, please check the [Database Documentation](https://github.com/a97u/Smart-Health-Management-System/wiki/Database-Schema).

## Testing with Postman

### Setup Instructions:

1. Install Postman from the [official website](https://www.postman.com/downloads/)
2. Import the `HealthCare-API.postman_collection.json` file
3. Set the environment variable `baseUrl` to `http://localhost:8080`

### Authentication in Postman:

- Select Basic Auth
- Enter the email and password used during registration

### Testing File Upload:

- Endpoint: `POST {{baseUrl}}/api/documents/upload`
- Auth: Basic Auth with patient credentials
- Form-data fields:
  - documentName: e.g., "Test Results"
  - documentType: e.g., "MEDICAL_REPORT"
  - description: e.g., "Regular checkup results"
  - file: Select Files & Upload a test file

### Testing File Download:

- Endpoint: `GET {{baseUrl}}/api/documents/download/{id}`
- Replace `{id}` with a valid document ID
- Auth: Basic Auth with valid credentials
- Expect a downloadable file with HTTP status 200

## Future Enhancements

- Integrate JWT for enhanced security
- Implement online payment gateway for seamless billing
- Develop a responsive UI for smoother user experience

---

© 2025 Smart Health Management System. All rights reserved.

GitHub: [https://github.com/a97u/Smart-Health-Management-System](https://github.com/a97u/Smart-Health-Management-System)
