#Seyren ([/ˈsaɪ.rʌn/](http://en.wikipedia.org/wiki/Wikipedia:IPA_for_English#Key))

An alerting dashboard for Graphite

<img src="http://i.imgur.com/hyAEH.png" height="490" width="800" />

##Run

###Prerequisites

* Maven
* An instance of Graphite
* Mongodb ([Install instructions](http://docs.mongodb.org/manual/installation/#installation-guides Installing MongoDB))

###Stand alone

```
export GRAPHITE_URL=http://graphite.foohost.com:80
mvn clean package
java -jar seyren-web/target/dependency/jetty-runner.jar --path /seyren seyren-web/target/*.war
open http://localhost:8080/seyren
```

###Available environment variables
* `GRAPHITE_URL` - The location of your graphite server. Default: `http://localhost:80`
* `GRAPHITE_USERNAME` - The Http Basic auth username for the graphite server. Default: ``
* `GRAPHITE_PASSWORD` - The Http Basic auth password for the graphite server. Default: ``
* `MONGO_URL` - The mongo connection string. Default: `mongodb://localhost:27017/seyren`
* `PAGERDUTY_DOMAIN` - The PagerDuty domain to be notified. Default: ``
* `SEYREN_URL` - The location of your seyren instance. Default: `http://localhost:8080/seyren`
* `SEYREN_FROM_EMAIL` - The from email address for sending out notifications. Default: `alert@seyren`
* `SMTP_HOST` - The smtp server to send email notifications from. Default: `localhost`
* `SMTP_PORT` - The smtp server port. Default: `25`
* `SMTP_USERNAME` - The smtp server username if authenticated SMTP is used. Default: ``
* `SMTP_PASSWORD` - The smtp server password if authenticated SMTP is used. Default: ``

###Cloud Formation

If you are running on amazon infrastructure use this [Cloud Formation Template](https://gist.github.com/3933244) to bring up a single instance of any size. All the environment variables required for Seyren are specified as properties to the cloud formation template and a fully configured Seyren instance should come up with no other intervention.

##Development

To run the acceptance tests with Maven:

```
mvn clean verify
```

To fire-up the app using Maven and wait (meaning you can run the tests separately from your IDE):

```
mvn clean verify -Dwait
```

You should then be able to browse to `http://localhost:8080/seyren` and have a play.
