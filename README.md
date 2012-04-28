To run the acceptance tests with Maven:

```
mvn clean verify
```

To fire-up the app using Maven (meaning you can run the tests separately from your IDE):

```
mvn clean verify -Dwait
```

To run stand alone:

```
java -jar graphite-siren-web/target/dependency/jetty-runner.jar graphite-siren-web/target/*.war
```
