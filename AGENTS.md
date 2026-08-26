# AGENTS.md

Email delivery API docs: [GOV.UK Notify](https://www.notifications.service.gov.uk/)

## Architecture

- App module: `laa-data-claims-notify-service`
- Main flow: consume SNS/SQS messages and send emails via GOV.UK Notify
- Related services:
  - All available at `../`
  - `laa-data-claims-api` - claims data store - [repo](https://github.com/ministryofjustice/laa-data-claims-api)
  - `laa-data-claims-event-service` - [repo](https://github.com/ministryofjustice/laa-data-claims-event-service)
- Supporting infra: LocalStack, SNS, SQS

## Code standards

- Follow existing Spring Boot and mapper patterns.
- Java uses **Google Java Format** via Spotless.
- Run:

```sh
./gradlew spotlessApply checkstyleAll
```

## Testing

Only run the required tests.

```sh
./gradlew :laa-data-claims-notify-service:test
./gradlew :laa-data-claims-notify-service:integrationTest
./gradlew :laa-data-claims-notify-service:pactTest
```
