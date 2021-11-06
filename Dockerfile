FROM maven:3.6.2-jdk-8 AS build

COPY . /home/

RUN cd /home/ && \
	mvn clean package

FROM openjdk:8-jre-alpine

COPY --from=build "/home/target/tttbot-*.jar" "/home/tttbot.jar"

WORKDIR "/home/"

ENTRYPOINT ["java", "-jar", "tttbot.jar" ]
CMD [ "env" ]