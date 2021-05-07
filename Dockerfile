FROM openjdk:17-jdk-slim
EXPOSE 8080
VOLUME /tmp
COPY target/*.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
HEALTHCHECK --interval=1m --timeout=3s CMD wget -q -T 3 -s http://localhost:8080
