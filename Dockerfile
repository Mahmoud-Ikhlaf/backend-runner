FROM openjdk:21

EXPOSE 8081

ADD target/mahoot-images.jar mahoot-images.jar

ENTRYPOINT ["java", "-jar", "/mahoot-images.jar"]