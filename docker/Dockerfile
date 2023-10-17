# Dockerfile
### base image (슬림버전으로 교체)
FROM openjdk:17-jdk-slim
### jar file 위치
ARG JAR_FILE=*.jar
### 파일 복사
COPY ${JAR_FILE} app.jar
### 실행할 명령 지정한다.
ENTRYPOINT ["java","-jar","/app.jar"]
