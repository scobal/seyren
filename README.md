To run the acceptance tests with Maven:

```
$ mvn clean verify
```

To fire-up the app using Maven (meaning you can run the tests separately from your IDE):

```
$ mvn clean verify -Dwait
```

You should then be able to browse to `http://localhost:8080/seyren` and have a play.

To run stand alone:

```
$ export GRAPHITE_URL=http://graphite.foohost.com:80
$ mvn clean package
$ java -jar seyren-web/target/dependency/jetty-runner.jar seyren-web/target/*.war
```

List of all environment variables
* GRAPHITE_URL - The location of your graphite server. Default: `http://localhost:80`
* SMTP_HOST - The smtp server to send email notifications from. Default: `localhost`
* SMTP_PORT - The smtp server port. Default: `25`
