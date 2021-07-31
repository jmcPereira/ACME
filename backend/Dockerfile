FROM adoptopenjdk/openjdk16:latest
WORKDIR /acme
COPY . .
RUN apt-get update && apt-get install dos2unix
RUN chmod +x mvnw
RUN dos2unix mvnw
CMD ./mvnw clean package spring-boot:repackage && java -jar ./target/ACME-0.0.1-SNAPSHOT.jar
EXPOSE 3000
