FROM gradle:7.2.0-jdk11 AS builderrr
ENV POSTGRES_HOST=${POSTGRES_HOST}, POSTGRES_USERNAME=${POSTGRES_USERNAME}, POSTGRES_PASSWORD=${POSTGRES_PASSWORD}, REDIS_HOST=${REDIS_HOST}, GOOGLE_APPLICATION_CREDENTIALS="/run/secrets/firebase_admin_creds"
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
CMD gradle clean build --no-daemon --stacktrace

FROM adoptopenjdk/openjdk11:alpine AS final
EXPOSE 8080
VOLUME /tmp
WORKDIR /app
COPY --from=builderrr /home/gradle/src/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=dev","-jar","/app/app.jar"]
