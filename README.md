To run with maven:

mvn clean verify
mvn clean verify -Dwait

To run stand alone:

java -jar graphite-siren-web/target/dependency/jetty-runner.jar graphite-siren-web/target/*.war

