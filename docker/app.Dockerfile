# ===== build stage =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -q -DskipTests package &&     mkdir -p /out &&     cp adapters/web/target/*.jar /out/app.jar

# ===== runtime stage =====
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /out/app.jar ./app.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]