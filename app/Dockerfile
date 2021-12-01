FROM gradle:7.2.0-jdk11 AS cacheddeps
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY --chown=gradle:gradle build.gradle.kts /home/gradle/src/
WORKDIR /home/gradle/src
RUN gradle clean build -i --stacktrace -x bootJar

FROM gradle:7.2.0-jdk11 AS builderrr
COPY --from=cacheddeps /home/gradle/cache_home /home/gradle/.gradle
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle bootJar -i --stacktrace

FROM --platform=linux/amd64 adoptopenjdk/openjdk11:alpine AS final
EXPOSE 8080
VOLUME /tmp
WORKDIR /app
COPY --from=builderrr /home/gradle/src/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=dev","-jar","/app/app.jar"]
