FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/UrlShortenerPaymentService.jar .

ENV SPRING_PROFILES_ACTIVE=prod

CMD ["java", "-jar", "UrlShortenerPaymentService.jar"]
