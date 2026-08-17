# 构建阶段（Temurin 变体持续维护，openjdk:8 官方标签已停发）
FROM maven:3.8-eclipse-temurin-8 AS build
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:8-jre-alpine
WORKDIR /app
COPY --from=build /build/ruoyi-admin/target/ruoyi-admin.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
