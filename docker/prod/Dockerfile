FROM azul/zulu-openjdk-alpine:11

WORKDIR /app

COPY ["web/build/libs/web-*.jar", "./alignment-web.jar"]
COPY ["./entrypoint.sh", "/entrypoint.sh"]

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]