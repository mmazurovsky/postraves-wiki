FROM gradle:7.2.0-jdk11 AS builderrr
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
CMD gradle clean build --no-daemon --stacktrace

FROM adoptopenjdk/openjdk11:alpine AS final
EXPOSE 8080
VOLUME /tmp
WORKDIR /app
COPY --from=builderrr /home/gradle/src/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=dev","-jar","/app/app.jar"]
