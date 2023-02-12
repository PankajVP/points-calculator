FROM openjdk:17-alpine
RUN mkdir -p main
RUN mkdir -p configs
VOLUME /tmp
EXPOSE 8080
ADD build/libs/points-calculator-1.0.0.jar points-calculator.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/points-calculator.jar"]