FROM gradle:6.9-jdk11-hotspot AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x test

FROM azul/zulu-openjdk-alpine:11

COPY --from=build ["/home/gradle/src/web/build/libs/web-*.jar", "app/alignment-web.jar"]
COPY ["docker/prod/entrypoint.sh", "/entrypoint.sh"]

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]