# Platform Backend

A microservices-based backend platform for RADAR-base built with Kotlin and Jersey.

## Project Structure

- `delegate-api`: Main API gateway that delegates requests to other microservices
- `user-service`: Handles user management and authentication
- `data-sources-service`: Manages data source connections and configurations
- `data-service`: Handles data processing and storage

## Getting Started

1. Clone the repository
2. Build the project:
   ```bash
   ./gradlew build
   ```
3. Run individual services:
   ```bash
   ./gradlew :delegate-api:run
   ./gradlew :project-service:run
   ./gradlew :user-service:run
   ./gradlew :data-sources-service:run
   ./gradlew :config-service:run
   ```
