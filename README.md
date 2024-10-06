# Student Management System

## Overview

The **Student Management System** is a Spring Boot-based project designed to streamline student and course management. It allows users to register, log in, and manage their course registrations through a simple RESTful API. The system supports features such as caching for performance enhancement and security through JWT-based authentication.

Key Features:
- **Caching**: Efficiently caches data using [Ehcache](https://www.ehcache.org/) for optimized performance.
- **Database**: Integrated with PostgreSQL for persistent storage.
- **JWT Security**: Utilizes JSON Web Tokens (JWT) for secure authentication. Tokens expire every 5 minutes, ensuring enhanced security.
- **Course Schedule**: Exports a student's course schedule as a PDF document with custom style.
- **Input Validation** ensure the inputs of request body are valid
- **Testing** 

## Prerequisites

Before setting up the project, ensure you have the following installed:

1. **Maven**: Build tool to manage dependencies.
2. **Java 17**: The project requires Java 17 to run.
3. **PostgreSQL**: Set up a PostgreSQL instance as the database.
4. **Corretto JDK 17**: Ensure you have the correct JDK version from Amazon Corretto.

## Getting Started

Follow these steps to set up the project:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-repo/student-management-system.git
   cd student-management-system
   ```

2. **Configure Environment Variables**:
    - Set your PostgreSQL database credentials in the `application.properties` file.
    - Define the `security.jwt.secret-key` environment variable. This key must be a 256-bit HMAC hash string. You can generate a key using [this online tool](https://www.devglan.com/online-tools/hmac-sha256-online?ref=blog.tericcabrel.com).

   Example `.env` file:
   ```bash
   DB_URL=jdbc:postgresql://localhost:5432/your_db
   DB_USERNAME=your_db_user
   DB_PASSWORD=your_db_password
   JWT_SECRET=your_generated_secret_key
   ```
I am leaving security.jwt.secret-key on purpose to make it fast since it's not production.

## API Endpoints

Notes:
    1. All endpoints need Bearer Token as authorization header. You can get one by sign-up or login.
    2. To create a course, must have role ADMIN. The default when sign up is USER

### 1. **User Registration**
- **Endpoint**: `POST /api/auth/signup`
- **Request Body**:
  ```json
  {
    "username": "testuser",
    "password": "testpassword",
    "email": "testuser@example.com",
    "firstName": "Test",
    "lastName": "User",
    "dateOfBirth": "1990-01-01",
    "phoneNumber": "1234567890",
    "address": "123 Test Street, Test City, 12345"
  }
  ```
- **Response**:
  ```json
  {
    "token": "JWT_TOKEN",
    "expiresIn": 300000
  }
  ```

### 2. **User Login**
- **Endpoint**: `POST /api/auth/login`
- **Request Body**:
  ```json
  {
    "username": "testuser",
    "password": "testpassword"
  }
  ```
- **Response**:
  ```json
  {
    "token": "JWT_TOKEN",
    "expiresIn": 300000
  }
  ```

### 3. **Create a Course**
- **Endpoint**: `POST /api/courses`
- **Request Body**:
  ```json
  {
    "name": "Introduction to Spring Boot",
    "description": "Learn the basics of Spring Boot",
    "startDate": "2023-09-01T09:00:00",
    "endDate": "2023-12-15T17:00:00"
  }
  ```
- **Response**:
  ```json
  {
    "id": 4,
    "name": "Introduction to Spring Boot",
    "description": "Learn the basics of Spring Boot",
    "startDate": "2023-09-01T09:00:00",
    "endDate": "2023-12-15T17:00:00",
    "students": []
  }
  ```

### 4. **Get All Courses**
- **Endpoint**: `GET /api/courses`
- **Response**:
  ```json
  [
    {
      "id": 1,
      "name": "Course Name",
      "description": "Course Description",
      "startDate": "2023-12-31T23:59:59",
      "endDate": "2023-09-01T00:00:00",
      "students": [
        {
          "id": 1,
          "username": "testuser",
          "email": "testuser@example.com",
          "firstName": "Test",
          "lastName": "User"
        }
      ]
    },
    {
      "id": 2,
      "name": "Introduction to Spring Boot",
      "description": "Learn the basics of Spring Boot",
      "startDate": "2023-09-01T09:00:00",
      "endDate": "2023-12-15T17:00:00",
      "students": []
    }
  ]
  ```

### 5. **Register a Student to a Course**
- **Endpoint**: `POST /api/courses/{courseId}/register`
- **Response**: `200 OK`

### 6. **Cancel Course Registration**
- **Endpoint**: `DELETE /api/courses/{courseId}/cancel`
- **Response**: `200 OK`

### 7. **Get Course Schedule as PDF**
- **Endpoint**: `GET /api/courses/schedule`
- **Response**: PDF file containing the course schedule.

## Caching

The project leverages Ehcache to cache frequently accessed data and improve performance. Two caches are used:

1. **Courses Cache**: Caches the list of all courses for 15 minutes.
2. **Course Schedule Cache**: Caches the course schedule PDF for 30 minutes per user.

### Caching Configuration Example

Caching is configured in `ehcache.xml`. You can adjust cache expiry time and memory allocation as needed:

```xml

<cache alias="courses">
    <key-type>java.lang.String</key-type>
    <value-type>java.util.List</value-type>
    <expiry>
        <ttl unit="minutes">15</ttl>
    </expiry>
    <resources>
        <heap unit="entries">100</heap>
        <offheap unit="MB">10</offheap>
    </resources>
</cache>

<cache alias="courseSchedule">
<key-type>java.lang.Long</key-type>
<value-type>byte[]</value-type>
<expiry>
    <ttl unit="minutes">30</ttl>
</expiry>
<resources>
    <heap unit="entries">50</heap>
    <offheap unit="MB">20</offheap>
</resources>
</cache>
```

## Security

JWT-based authentication secures the application. The **JWT tokens** expire after 5 minutes, requiring users to re-authenticate for continued access.

To customize the security settings, modify the `application.properties` and set your JWT secret key, which should be an HMAC SHA-256 hash string.

