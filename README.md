# laa-data-claims-notify-service

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/laa-data-claims-notify-service/badge)](https://github-community.service.justice.gov.uk/repository-standards/laa-data-claims-notify-service)

## Overview

`laa-data-claims-notify-service` is an intermediary notification service within the LAA Data Claims
domain.

It consumes messages from an AWS SNS topic and uses those messages to dispatch emails
via [GOV.UK Notify](https://www.notifications.service.gov.uk/). This decouples email delivery from
individual upstream services — any service that needs to send a notification publishes an event to
SNS, and this service handles delivery.

**Benefits of this pattern:**

- Centralised email delivery and template management
- Upstream services have no dependency on GOV.UK Notify directly
- New notification channels (e.g. SMS, letters) can be added here without changes to upstream
  services
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
│   │   └── pactTest/                    # PACT tests
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

# Pact Tests
./gradlew pactTest
```

# TODO: Add documentation on SNS and SQS setup. Event service already has config for setting up a localstack instance and processing queue. This probably should be amended to also create a second queue and new SNS to keep it all together.

Run the service locally:

```bash
./gradlew :laa-data-claims-notify-service:bootRun
```

Run with the local Spring profile (human-readable console logging, no ECS structured output):

```bash
./gradlew :laa-data-claims-notify-service:bootRun --args='--spring.profiles.active=local'
```

### Via docker compose

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

## Local Endpoints

| Service         | URL                                     |
|-----------------|-----------------------------------------|
| Actuator health | `http://localhost:8183/actuator/health` |
| Remote debug    | `localhost:5083`                        |

## Configuration

The main configuration file is:

```
laa-data-claims-notify-service/src/main/resources/application.yml
```

### Key environment variables

| Variable                          | Description                                  | Default                                        |
|-----------------------------------|----------------------------------------------|------------------------------------------------|
| `SPRING_PROFILES_ACTIVE`          | Active Spring profile                        | —                                              |
| `AWS_REGION`                      | AWS region                                   | `eu-west-2`                                    |
| `CLAIMS_API_URL`                  | Base URL of the Claims API                   | `http://localhost:8080`                        |
| `CLAIMS_API_ACCESS_TOKEN`         | Access token for the Claims API              | (test token — override in deployed envs)       |
| `AWS_SQS_NOTIFY_QUEUE_NAME`       | Name of the SQS queue to consume from        | `notify-queue`                                 |
| `NOTIFY_API_KEY`                  | GOV.UK Notify API key                        | `123` (invalid — must be set in deployed envs) |
| `ROOT_LOGGING_LEVEL`              | Root log level                               | `info`                                         |
| `SPRING_LOGGING_LEVEL`            | Spring framework log level                   | `info`                                         |
| `APP_LOGGING_LEVEL`               | Application log level                        | `info`                                         |
| `sentry.dsn`                      | Sentry DSN for error tracking                | —                                              |
| `sentry.environment`              | Sentry environment label                     | —                                              |

## Logging

Structured [ECS-format](https://www.elastic.co/guide/en/ecs/current/index.html) logging is enabled
by default, suitable for ingestion by a log aggregation platform.

When running with `--spring.profiles.active=local` (or the `local` Docker Compose profile),
structured logging is disabled in favour of a human-readable console format with trace/span IDs
included.

Code coverage is tracked via Jacoco. The main application entry point (
`LaaDataClaimsNotifyServiceApplication`) is excluded from coverage analysis. Coverage reports are
generated automatically after the `test` task completes.

> **Note:** The unit test context load disables AWS SQS (`spring.cloud.aws.sqs.enabled=false`) to
> allow the Spring context to start without a live AWS connection.

## CI/CD

GitHub Actions pipelines are defined in `.github/workflows/`:

| Workflow                 | Trigger                    | Purpose                                                                                                |
|--------------------------|----------------------------|--------------------------------------------------------------------------------------------------------|
| `deploy-uat-preview.yml` | Pull request (all events)  | Build, test, publish preview image to ECR, deploy ephemeral UAT preview environment and post URL to PR |
| `build-main.yml`         | PR merged to `main`        | Build, test, publish versioned artefact to GitHub Packages, create Git tag                             |
| `deploy-main.yml`        | Tag push                   | Assemble, publish image to ECR, deploy sequentially to UAT → Staging → Production via Helm             |
| `helm-deploy.yml`        | Reusable (called by above) | Authenticate to cluster, run `helm upgrade --install`, update ECR image tags                           |

Semgrep static analysis is run as part of the build and test workflows. Deployments use Helm with a
5-minute rollout timeout per environment.

## Local Snyk container scanning

You can run the same style of container scan locally with the Snyk CLI against a locally built OCI
image.

### Prerequisites

- Docker
- [Snyk CLI](https://docs.snyk.io/developer-tools/snyk-cli)
- Snyk authentication via either `SNYK_TOKEN` or `SNYK_CLIENT_ID` and `SNYK_CLIENT_SECRET`

### Run the scan

```bash
./scripts/snyk-container-scan.sh
```

This script:

1. Builds a local image with `:laa-data-claims-notify-service:bootBuildImage`
2. Reuses the repository `.snyk` policy file
3. Runs `snyk container test` with a default severity threshold of `high`

Useful variants:

```bash
# Scan an already-built image
./scripts/snyk-container-scan.sh --skip-build

# Change the image name or severity threshold
./scripts/snyk-container-scan.sh --image-name laa-data-claims-notify-service:test --severity-threshold critical

# Pass extra Snyk CLI flags through after --
./scripts/snyk-container-scan.sh -- --json
```

If Snyk returns an auth-related `SNYK-0003`, your local CLI session has usually expired. Re-run
`snyk auth`, or export fresh `SNYK_TOKEN` or `SNYK_CLIENT_ID` and `SNYK_CLIENT_SECRET` values
before running the script again.

## Code Style and Formatting

[Spotless](https://github.com/diffplug/spotless) is used to enforce consistent Java formatting. It
applies Google Java Format and removes unused imports automatically.

Apply formatting:

```bash
./gradlew spotlessApply
```

Check formatting without modifying files:

```bash
./gradlew spotlessCheck
```

Spotless runs automatically on staged Java files via the pre-commit hook (see below), so formatting
is applied before each commit.

## Pre-commit Hooks

This project uses [`prek`](https://github.com/ministryofjustice/devsecops-hooks) to manage
pre-commit hooks. Run the setup script once after cloning:

```bash
./scripts/setup-hooks.sh
```

This installs `prek` and activates the hooks defined in `.pre-commit-config.yaml`. On each commit
the following checks run automatically:

| Hook                           | What it does                                                   |
|--------------------------------|----------------------------------------------------------------|
| **Spotless**                   | Applies Google Java Format to staged Java files                |
| **Checkstyle**                 | Validates code style against Google Java Style rules           |
| **GitHub Actions SHA pinning** | Ensures external Actions are pinned to full-length commit SHAs |
| **MoJ baseline scanner**       | Runs Ministry of Justice DevSecOps baseline security checks    |

You can also trigger the hooks manually against all files:

```bash
prek run --all-files
```

## Commit Signing

All commits must be GPG-signed. Configure Git to sign commits automatically:

```bash
git config --global commit.gpgsign true
```

If you do not have a GPG key set up, follow
the [GitHub guide on generating a GPG key](https://docs.github.com/en/authentication/managing-commit-signature-verification/generating-a-new-gpg-key)
and add it to your GitHub account before contributing.

## Contributing

- Branch from `main` and open a pull request
- Run the hook setup script (`./scripts/setup-hooks.sh`) after cloning so pre-commit checks run
  locally
- Ensure all unit and integration tests pass: `./gradlew clean build integrationTest`
- All commits must be GPG-signed
- Keep changes covered by appropriate tests before raising a PR
