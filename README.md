# Church Management Application

A RESTful API built with Spring Boot for managing church operations including members, families, events, donations, and more.

## Technology Stack

- **Spring Boot 3.2.0** - Main framework
- **Spring Data JPA** - Database access layer
- **Spring Security** - Authentication and authorization
- **Apache Camel** - Integration framework for routing and async processing
- **Vavr** - Functional programming library for immutable collections and error handling
- **PostgreSQL** - Database
- **Flyway** - Database migrations
- **Lombok** - Reduce boilerplate code
- **MapStruct** - Object mapping
- **Gradle** - Build tool

## Project Structure

```
src/
├── main/
│   ├── java/com/churchapp/
│   │   ├── Application.java                 # Main Spring Boot application
│   │   ├── config/
│   │   │   ├── CamelConfig.java            # Apache Camel routes configuration
│   │   │   └── DatabaseConfig.java         # Database configuration
│   │   ├── controller/
│   │   │   └── MemberController.java       # REST API endpoints
│   │   ├── dto/
│   │   │   └── MemberDTO.java              # Data Transfer Objects
│   │   ├── entity/
│   │   │   ├── Member.java                 # JPA entities
│   │   │   ├── Family.java
│   │   │   ├── Event.java
│   │   │   ├── Donation.java
│   │   │   ├── Group.java
│   │   │   ├── User.java
│   │   │   └── enums/
│   │   │       ├── DonationType.java
│   │   │       └── RoleType.java
│   │   ├── repository/
│   │   │   ├── MemberRepository.java       # Spring Data JPA repositories
│   │   │   ├── DonationRepository.java
│   │   │   └── ...
│   │   └── service/
│   │       ├── MemberService.java          # Business logic with Vavr FP
│   │       ├── DonationService.java
│   │       └── EmailService.java
│   └── resources/
│       ├── application.yml                 # Application configuration
│       └── db/migration/
│           └── V1__create_schema.sql       # Database schema
└── test/
    └── java/com/churchapp/
        └── service/
            └── MemberServiceTest.java      # Unit tests
```

## Key Features

### 1. **Functional Programming with Vavr**
- Uses `Try<T>` for error handling instead of exceptions
- `Option<T>` for handling nullable values
- `List<T>` for immutable collections
- Functional style in services and controllers

### 2. **Apache Camel Integration**
- Asynchronous event processing
- Email notifications for donations
- Audit logging
- Error handling routes

### 3. **RESTful API Design**
- CRUD operations for all entities
- Proper HTTP status codes
- Validation with Bean Validation
- Error handling with functional approach

### 4. **Database Design**
- PostgreSQL with proper relationships
- Flyway migrations for version control
- Optimized indexes for performance
- Audit timestamps on all entities

## Database Schema

The application manages the following entities:

- **Families** - Family units within the church
- **Members** - Individual church members
- **Events** - Church events and activities
- **EventRegistrations** - Member registrations for events
- **Attendance** - Event attendance tracking
- **Donations** - Financial contributions with different types
- **Groups** - Small groups, committees, etc.
- **Volunteers** - Volunteer service tracking
- **Users** - System users with different roles

## Getting Started

### Prerequisites
- Java 17+
- PostgreSQL 12+
- Gradle 7+

### Setup

1. **Clone the repository**
```bash
git clone <repository-url>
cd church-app
```

2. **Set up PostgreSQL database**
```sql
CREATE DATABASE churchdb;
CREATE USER postgres WITH PASSWORD 'secret';
GRANT ALL PRIVILEGES ON DATABASE churchdb TO postgres;
```

3. **Configure application properties**
Update `src/main/resources/application.yml` with your database settings if needed.

4. **Build and run**
```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### API Endpoints

#### Members
- `GET /api/members` - Get all members
- `GET /api/members/{id}` - Get member by ID
- `GET /api/members/email/{email}` - Get member by email
- `GET /api/members/search?term={searchTerm}` - Search members
- `POST /api/members` - Create new member
- `PUT /api/members/{id}` - Update member
- `DELETE /api/members/{id}` - Delete member

#### Example Request
```bash
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "555-1234",
    "membershipDate": "2023-01-15"
  }'
```

## Development Notes

### Functional Programming Patterns

The application extensively uses Vavr for functional programming:

```java
// Service method using Try for error handling
public Try<Member> createMember(Member member) {
    return Try.of(() -> memberRepository.save(member))
        .onFailure(throwable -> log.error("Failed to create member", throwable));
}

// Controller handling Try results
return memberService.createMember(member)
    .fold(
        throwable -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error"),
        savedMember -> ResponseEntity.status(HttpStatus.CREATED).body(savedMember)
    );
```

### Apache Camel Routes

Donation events trigger email notifications asynchronously:

```java
from("direct:donationCreated")
    .log("Processing donation created event: ${body}")
    .to("bean:emailService?method=sendDonationNotification")
    .to("direct:auditLog");
```

### Testing

Run tests with:
```bash
./gradlew test
```

The application includes unit tests demonstrating Vavr usage:
```java
assertTrue(service.createMember(dto).isSuccess());
```

## Next Steps

1. **Implement Security (Task 7)** - Add JWT authentication
2. **Add more Controllers** - Events, Donations, etc.
3. **Enhance Camel Routes** - SMS notifications, external integrations
4. **Add Integration Tests** - Test complete workflows
5. **Implement Audit Service** - Complete audit logging
6. **Add API Documentation** - Swagger/OpenAPI

## Contributing

1. Follow the functional programming patterns established
2. Use Vavr `Try` for error handling
3. Write tests for all service methods
4. Update migrations for schema changes
5. Document new API endpoints

## License

This project is licensed under the MIT License.
