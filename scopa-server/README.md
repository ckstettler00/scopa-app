# scopa-server
## Overview
This project is a springboot application that exposes a REST apis and
a websocket interface to support playing the card game called scopa.

## Build
Using maven to build and run unit and cucumber test cases.
```
mvn clean install
```

## Execute
```
mvn spring-boot:run -Dspring-boot.run.profiles=testhelper

testhelper -- enables apis for simulated game events.

or 

java -jar target/scopa-server-{version}.jar
```
