FROM maven:3-jdk-11 as builder
WORKDIR /app

# prepare maven
COPY pom.xml .
COPY src ./src
RUN mvn clean install

FROM gcr.io/distroless/java-debian10:11
COPY --from=builder /app/target/*.war /app/app.war
WORKDIR /app
EXPOSE 7070
USER nonroot
ENTRYPOINT ["java","-jar","-Dserver.port=${port}", "-Dclient.oauth.user=${user}", "-Dclient.oauth.password=${pass}", "app.war"]