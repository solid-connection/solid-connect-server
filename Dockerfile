# JDK 버전 설정
FROM openjdk:17-jdk

# JAR_FILE 변수 정의
ARG JAR_FILE=./build/libs/solid-connection-0.0.1-SNAPSHOT.jar

# JAR 파일 메인 디렉토리에 복사
COPY ${JAR_FILE} app.jar

# 시스템 진입점 정의
ENTRYPOINT ["java", "-jar", "/app.jar"]

# 볼륨 설정
VOLUME /tmp
