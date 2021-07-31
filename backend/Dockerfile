FROM adoptopenjdk/openjdk16:latest

RUN mkdir acme && \
    cd acme && \
    mkdir app && \
    cd app
COPY . acme/app
CMD cd acme/app && ./mvnw clean package spring-boot:repackage && java -jar ./target/ACME-0.0.1-SNAPSHOT.jar

EXPOSE 3000
