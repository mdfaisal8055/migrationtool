### Dockerfile to create Running migrationtool Image
FROM java:8
ADD target/migrationtool*.jar migrationtool.jar
ENV JAVA_OPTS="-Xmx2048m -Xms512m -XX:NewSize=256m -XX:MaxNewSize=512m"
RUN bash -c 'touch /migrationtool.jar'
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/migrationtool.jar"]
 

