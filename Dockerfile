FROM maven:3.8.3-openjdk-17 AS build

COPY . /home/

RUN cd /home/ && \
	mvn clean package

FROM openjdk:14-alpine

COPY --from=build "/home/target/tttbot-*.jar" "/home/tttbot.jar"

WORKDIR "/home/"

ENTRYPOINT ["java", "-jar", "tttbot.jar" ]
CMD [ "env" ]