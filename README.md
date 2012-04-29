To run the acceptance tests with Maven:

```
mvn clean verify
```

To fire-up the app using Maven (meaning you can run the tests separately from your IDE):

```
mvn clean verify -Dwait
```

You should then be able to browse to `http://localhost:8080/seyren` and have a play.

To run stand alone:

```
java -jar seyren-web/target/dependency/jetty-runner.jar seyren-web/target/*.war
```
