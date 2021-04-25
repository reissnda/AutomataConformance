#!/bin/sh

[[ -z "${JAVA_OPTS}" ]] && JAVA_OPTS="-Xms512m -Xmx8g"

export JAVA_OPTS="${JAVA_OPTS} -XX:+UnlockExperimentalVMOptions -XX:+UseContainerSupport -Djava.security.egd=file:/dev/urandom"

exec java ${JAVA_OPTS} -jar app/alignment-web.jar