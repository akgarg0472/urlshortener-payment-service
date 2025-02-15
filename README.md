# URL Shortener Payment Service

![Java Version](https://img.shields.io/badge/Java-21-blue)
![version](https://img.shields.io/badge/version-1.5.2-blue)

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

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    status-page-url-path: /admin/management/info
    health-check-url-path: /admin/management/health

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
