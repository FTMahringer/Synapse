FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

# First build the plugin API module (required dependency)
COPY packages/core/pom.xml ./packages/core/pom.xml
COPY packages/core/src ./packages/core/src
COPY synapse-plugin-api/pom.xml ./synapse-plugin-api/pom.xml
COPY synapse-plugin-api/src ./synapse-plugin-api/src
RUN cd synapse-plugin-api && mvn -q install -DskipTests

# Then build the core module
RUN cd packages/core && mvn -q -Dmaven.test.skip=true package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/packages/core/target/synapse-core-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
