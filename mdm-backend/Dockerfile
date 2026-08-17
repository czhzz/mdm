# 构建阶段
FROM maven:3.8-openjdk-8 AS build
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /build/ruoyi-admin/target/ruoyi-admin.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
