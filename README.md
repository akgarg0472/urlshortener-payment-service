# URL Shortener Payment Service

![Java Version](https://img.shields.io/badge/Java-21-blue)
![version](https://img.shields.io/badge/version-1.7.4-blue)

## Introduction

The **Payment Service** is responsible for handling payments and processing billing for premium features in the URL
shortener application. Currently, this service integrates with **PayPal** as the sole payment gateway to manage
transactions. This service listens for payment events, processes them, and emit subscription events to kafka. It
is built using **Spring Boot**, **JPA**, **Kafka** and **MySQL**. The service is containerized using **Docker** for easy
deployment.

## Features

- **PayPal Payment Integration**: Processes payments via PayPal for unlocking premium features.
- **Event-Driven Notifications**: Sends notifications about payment status changes to other services using **Kafka**.
- **Subscription Management**: Updates user subscription status based on successful payments.
- **Containerized for Easy Deployment**: Fully dockerized to simplify deployment and scaling.

## Prerequisites

Ensure the following are set up before running the Payment Service:

- **MySQL** database with appropriate tables for managing payments and user subscriptions.
- **Kafka** for event-driven communication between services.
- **PayPal Developer Account**: To obtain API credentials for payment processing.
- **Docker** for containerization and simplified deployment.

## Installation

### Clone the Repository

```bash
git clone https://github.com/akgarg0472/urlshortener-payment-service
cd urlshortener-payment-service
```

### To compile and generate the JAR file, run:

```bash
./mvnw clean package -DskipTests
```

## Configuration

The **URL Shortener Payment Service** uses different configuration files for managing environment-specific
settings.

### application.yml

This is the main configuration file containing default settings for the application.

```yaml
server:
  port: 9390

spring:
  application:
    name: urlshortener-payment-service
  profiles:
    active: dev
  jackson:
    default-property-inclusion: non_null
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        service-name: ${spring.application.name}
        instance-id: ${spring.application.name}-${spring.application.instance_id:${random.value}}
        register: true
        fail-fast: true
        enabled: true
        prefer-ip-address: true
        catalog-services-watch-delay: 30000
        health-check-interval: 30s
        register-health-check: off
        health-check-path: /admin/management/health
        heartbeat:
          reregister-service-on-failure: true

paypal:
  order:
    cancel-url: http://localhost:3000/dashboard/paypal/cancel
    return-url: http://localhost:3000/dashboard/paypal/success

management:
  info:
    env:
      enabled: true
  endpoints:
    web:
      exposure:
        include: health,prometheus,info
      base-path: /admin/management
  endpoint:
    health:
      show-details: always
    info:
      access: read_only
    metrics:
      access: read_only
```

### Key Configurations

- **paypal.order.cancel-url**: Specifies the URL for redirecting users when they cancel the PayPal order.
- **paypal.order.return-url**: Specifies the URL for redirecting users after completing a PayPal order successfully.

## application-prod.yml

These are the configurations for production environment only.

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
  datasource:
    url: jdbc:mysql://localhost:3306/urlshortener?serverTimezone=UTC&createDatabaseIfNotExist=true
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      pool-name: PaymentServiceConnectionPool
      maximum-pool-size: 20
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: update
    generate-ddl: true
    open-in-view: false
    show-sql: false
  data:
    redis:
      host: localhost
      port: 6379
      database: 6
      password:

kafka:
  payment:
    success:
      topic:
        name: urlshortener.payment.events
        partitions: 1
        replication-factor: 1

subscription:
  cache:
    expiration:
      pack: 43200000
      active-plan: 300000

paypal:
  environment: Production  # Possible values: 'Production' or 'Sandbox'
  oauth:
    client-id: <your-client-id>  # Your PayPal OAuth client ID
    client-secret: <your-client-secret>  # Your PayPal OAuth client secret
```

### Key Configurations

- **kafka.payment.success.topic.name**: Specifies the name of the Kafka topic for payment success events.
- **kafka.payment.success.topic.partitions**: Defines the number of partitions for the Kafka topic.
- **kafka.payment.success.topic.replication-factor**: Sets the replication factor for the Kafka topic.
- **subscription.cache.expiration.pack**: Defines the expiration time for the "pack" cache in milliseconds.
- **subscription.cache.expiration.active-plan**: Defines the expiration time for the "active-plan" cache in
  milliseconds.
- **paypal.environment**: Specifies the environment for the PayPal integration. Possible values are **'Production'** or
  **'Sandbox'**.
- **paypal.oauth.client-id**: The client ID for OAuth authentication with PayPal.
- **paypal.oauth.client-secret**: The client secret for OAuth authentication with PayPal.

### Logging Configuration

The URL Shortener Payment Service uses environment variables for logging configuration. Below are the available environment
variables that you can customize:

- **LOGGING_CONSOLE_ENABLED**: Enables or disables console-based logging.
    - Default value: `false`
    - Allowed values: `true`, `false`

- **LOGGING_FILE_ENABLED**: Enables or disables file-based logging.
    - Default value: `false`
    - Allowed values: `true`, `false`

- **LOGGING_FILE_BASE_PATH**: Specifies the base path for log files.
    - Default value: `/tmp`

- **LOG_LEVEL**: Specifies the log level for the application.
    - Default value: `INFO`
    - Allowed values: `DEBUG`, `INFO`, `WARN`, `ERROR`

- **LOGGING_STREAM_ENABLED**: Enables or disables streaming logs.
    - Default value: `false`
    - Allowed values: `true`, `false`

- **LOGGING_STREAM_HOST**: Specifies the host for streaming logs.
    - Default value: `localhost`

- **LOGGING_STREAM_PORT**: Specifies the port for streaming logs.
    - Default value: `5000`

- **LOGGING_STREAM_PROTOCOL**: Specifies the protocol used for log streaming.
    - Default value: `TCP`
    - Allowed values: `TCP`, `UDP`

## Docker Setup

The application is Dockerized for simplified deployment. The `Dockerfile` is already configured to build and run the
Spring Boot application.

The `Dockerfile` defines the build and runtime configuration for the container.

### Building the Docker Image

To build the Docker image, run the following command:

```bash
docker build -t akgarg0472/urlshortener-payment-service:tag .
```

### Run the Docker Container

You can run the application with custom environment variables using the docker run command. For example:

```bash
docker run -p 9090:9090 \
           -e SPRING_PROFILES_ACTIVE=prod \
           akgarg0472/urlshortener-payment-service:tag
```

This will start the container with the necessary environment variables.
