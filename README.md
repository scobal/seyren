<img src="http://i.imgur.com/Ae5gQJZ.png" height="60" width="250" />

Seyren ([/ˈsaɪ.rʌn/](http://en.wikipedia.org/wiki/Wikipedia:IPA_for_English#Key)) is an alerting dashboard for Graphite.

[<img src="http://i.imgur.com/66lXcBk.png" height="465" width="700" />](http://i.imgur.com/XTpxmKI.png)

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

###Environment variables

#### Base
* `GRAPHITE_URL` - The location of your graphite server. Default: `http://localhost:80`
* `GRAPHITE_USERNAME` - The Http Basic auth username for the graphite server. Default: ``
* `GRAPHITE_PASSWORD` - The Http Basic auth password for the graphite server. Default: ``
* `GRAPHITE_KEYSTORE` - The Http KeyStore path for the https graphite server. Default: ``
* `GRAPHITE_KEYSTORE_PASSWORD` - The Http KeyStore password for the https graphite server. Default: ``
* `GRAPHITE_TRUSTSTORE` - The Http TrustStore path for the https graphite server. Default: ``
* `MONGO_URL` - The mongo connection string. Default: `mongodb://localhost:27017/seyren`
* `SEYREN_URL` - The location of your seyren instance. Default: `http://localhost:8080/seyren`

#### SMTP
* `SMTP_HOST` - The smtp server to send email notifications from. Default: `localhost`
* `SMTP_PORT` - The smtp server port. Default: `25`
* `SMTP_FROM` - The from email address for sending out notifications. Default: `alert@seyren`
* `SMTP_USERNAME` - The smtp server username if authenticated SMTP is used. Default: ``
* `SMTP_PASSWORD` - The smtp server password if authenticated SMTP is used. Default: ``
* `SMTP_PROTOCOL` - The smtp server protocol if authenticated SMTP is used. Default: `smtp`

#### HipChat
* `HIPCHAT_AUTHTOKEN` - The hipchat api auth token. Default: ``
* `HIPCHAT_USERNAME` - The username that messages will be sent from. Default: `Seyren Alert`

#### PagerDuty
* `PAGERDUTY_DOMAIN` - The PagerDuty domain to be notified. Default: ``
* `PAGERDUTY_USERNAME` - The PagerDuty API username. Default: ``
* `PAGERDUTY_PASSWORD` - The PagerDuty API Password. Default: ``

#### Hubot
* `HUBOT_URL` - The location where Hubot is running. Default ``

#### Flowdock
* `FLOWDOCK_EXTERNAL_USERNAME` - The username that messages will be sent from to a flow. Default: `Seyren`
* `FLOWDOCK_TAGS` -  Special tags to add to all messages. Default: ``
* `FLOWDOCK_EMOJIS` - Mapping between state and emojis unicode. Default: ``

###Cloud Formation

If you are running on amazon infrastructure use this [Cloud Formation Template](https://gist.github.com/5922231) to bring up a single instance of any size. All the environment variables required for Seyren are specified as properties to the cloud formation template and a fully configured Seyren instance should come up with no other intervention.

##Development

[![Build Status](https://secure.travis-ci.org/scobal/seyren.png?branch=master)](http://travis-ci.org/scobal/seyren)

To run the acceptance tests with Maven:

```
mvn clean verify
```
To run the integration tests with Maven:

```
mvn clean verify -Pkarma
```

To fire-up the app using Maven and wait (meaning you can run the tests separately from your IDE):

```
mvn clean verify -Dwait
```

You should then be able to browse to `http://localhost:8080/seyren` and have a play.

