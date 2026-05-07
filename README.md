# laa-data-claims-notify-service
[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/laa-data-claims-notify-service/badge)](https://github-community.service.justice.gov.uk/repository-standards/laa-data-claims-notify-service)

## Overview

`laa-data-claims-notify-service` is an intermediary notification service within the LAA Data Claims domain.

It consumes messages from an AWS SNS topic and uses those messages to dispatch emails via [GOV.UK Notify](https://www.notifications.service.gov.uk/). This decouples email delivery from individual upstream services — any service that needs to send a notification publishes an event to SNS, and this service handles delivery.

**Benefits of this pattern:**
- Centralised email delivery and template management
- Upstream services have no dependency on GOV.UK Notify directly
- New notification channels (e.g. SMS, letters) can be added here without changes to upstream services
- Consistent observability and error handling across all outbound notifications

### How it works

```
Upstream Service(s)
       │
       │  publish event
       ▼
   AWS SNS Topic
       │
       │  consume message
       ▼
laa-data-claims-notify-service
       │
       │  send email
       ▼
   GOV.UK Notify
```

### Project Structure

This is a multi-module Gradle project:

```
laa-data-claims-notify-service/          # repository root
├── laa-data-claims-notify-service/      # Spring Boot application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── uk/gov/justice/laa/dstew/payments/notify/
│   │   │   └── resources/
│   │   │       └── application.yml      # application configuration
│   │   ├── test/                        # unit tests
│   │   └── integrationTest/             # integration tests
│   └── build.gradle
├── config/
│   └── checkstyle/
│       └── checkstyle.xml               # Google Java Style rules
├── .github/workflows/                   # CI/CD pipelines
├── Dockerfile
├── docker-compose.yml
├── build.gradle
└── settings.gradle
```

## Prerequisites

- Java 25 (Amazon Corretto)
- Docker and Docker Compose

## Building and Running the Application

### Via gradlew

Build the application:

```bash
./gradlew clean build
```

Run tests:

```bash
# Unit tests
./gradlew test

# Integration tests
./gradlew integrationTest

# Both
./gradlew test integrationTest
```

Run the service locally:

```bash
./gradlew :laa-data-claims-notify-service:bootRun
```

Run with the local Spring profile (human-readable console logging, no ECS structured output):

```bash
./gradlew :laa-data-claims-notify-service:bootRun --args='--spring.profiles.active=local'
```

### Via docker compose (which includes localstack setup dependency for SNS)

The `docker-compose.yml` starts the service alongside a [LocalStack](https://docs.localstack.cloud/) container, which emulates AWS SNS locally.

Start all services:

```bash
docker compose up --build
```

Start in detached mode:

```bash
docker compose up --build -d
```

Stop and remove containers:

```bash
docker compose down
```

> **Note:** On first start, LocalStack will create the SNS topic defined in the init scripts. Messages can then be published to the local SNS endpoint at `http://localhost:4566`.

## Local Endpoints

| Service | URL |
|---|---|
| Application | `http://localhost:8083` |
| Actuator health | `http://localhost:8183/actuator/health` |
| Remote debug | `localhost:5083` |
| LocalStack (AWS SNS) | `http://localhost:4566` |

## Configuration

The main configuration file is:

```
laa-data-claims-notify-service/src/main/resources/application.yml
```

### Key environment variables

| Variable | Description | Default |
|---|---|---|
| `SERVER_PORT` | Application port | `8083` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | — |
| `ROOT_LOGGING_LEVEL` | Root log level | `info` |
| `SPRING_LOGGING_LEVEL` | Spring framework log level | `info` |
| `APP_LOGGING_LEVEL` | Application log level | `info` |
| `sentry.dsn` | Sentry DSN for error tracking | — |
| `sentry.environment` | Sentry environment label | — |

## Logging

Structured [ECS-format](https://www.elastic.co/guide/en/ecs/current/index.html) logging is enabled by default, suitable for ingestion by a log aggregation platform.

When running with `--spring.profiles.active=local` (or the `local` Docker Compose profile), structured logging is disabled in favour of a human-readable console format with trace/span IDs included.

## Testing

Tests are split into two source sets:

| Source set | Location | Purpose |
|---|---|---|
| Unit tests | `src/test/java` | Fast, isolated tests; Spring context load verification |
| Integration tests | `src/integrationTest/java` | Full Spring context tests, including actuator endpoint checks |

Run tests individually:

```bash
./gradlew test           # unit tests
./gradlew integrationTest  # integration tests
```

Code coverage is tracked via Jacoco. The main application entry point (`LaaDataClaimsNotifyServiceApplication`) is excluded from coverage analysis. Coverage reports are generated automatically after the `test` task completes.

> **Note:** The unit test context load disables AWS SQS (`spring.cloud.aws.sqs.enabled=false`) to allow the Spring context to start without a live AWS connection.

## CI/CD

GitHub Actions pipelines are defined in `.github/workflows/`:

| Workflow | Trigger | Purpose |
|---|---|---|
| `deploy-uat-preview.yml` | Pull request (all events) | Build, test, publish preview image to ECR, deploy ephemeral UAT preview environment and post URL to PR |
| `build-main.yml` | PR merged to `main` | Build, test, publish versioned artefact to GitHub Packages, create Git tag |
| `deploy-main.yml` | Tag push | Assemble, publish image to ECR, deploy sequentially to UAT → Staging → Production via Helm |
| `helm-deploy.yml` | Reusable (called by above) | Authenticate to cluster, run `helm upgrade --install`, update ECR image tags |

Semgrep static analysis is run as part of the build and test workflows. Deployments use Helm with a 5-minute rollout timeout per environment.

## Contributing

- Branch from `main` and open a pull request
- Ensure all unit and integration tests pass: `./gradlew clean build integrationTest`
- Code style is enforced via Checkstyle (Google Java Style, 100-character line limit) — checked automatically on build
- Keep changes covered by appropriate tests before raising a PR
