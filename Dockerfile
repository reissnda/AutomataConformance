FROM azul/zulu-openjdk-alpine:11

WORKDIR /app

COPY ["web/build/libs/web-*.jar", "./app/alignment-web.jar"]
COPY ["docker/entrypoint.sh", "/entrypoint.sh"]

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]