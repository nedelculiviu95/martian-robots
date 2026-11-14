FROM eclipse-temurin:21-jdk AS base

WORKDIR /build

COPY main/gradlew main/build.gradle main/settings.gradle ./
COPY main/gradle ./gradle
COPY main/src ./src

RUN chmod +x gradlew

FROM base AS test

RUN ./gradlew --no-daemon testClasses

FROM base AS build

RUN ./gradlew --no-daemon installDist

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/build/install/martian-robots/ ./

USER 10001:10001

ENTRYPOINT ["/app/bin/martian-robots"]
