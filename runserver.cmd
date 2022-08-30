@echo off
echo Staring springboot server.
(cd scopa-engine && mvn clean install -DskipTests && cd ../scopa-server && mvn spring-boot:run)
