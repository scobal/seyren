<img src="http://i.imgur.com/Ae5gQJZ.png" height="60" width="250" />

Seyren ([/ˈsaɪ.rʌn/](http://en.wikipedia.org/wiki/Wikipedia:IPA_for_English#Key)) is an alerting dashboard for Graphite.

[<img src="http://i.imgur.com/v9dJSwa.png" height="452" width="700" />](http://i.imgur.com/v9dJSwa.png)

##Run

###Prerequisites

* Maven
* An instance of Graphite
* Mongodb ([Install instructions](http://docs.mongodb.org/manual/installation/#installation-guides Installing MongoDB))

###Stand alone

```
mvn clean package
export GRAPHITE_URL=http://graphite.foohost.com:80
java -jar seyren-web/target/seyren-web-*-war-exec.jar
open http://localhost:8080
```

###Environment variables

#### Base
* `GRAPHITE_URL` - The location of your Graphite server. Default: `http://localhost:80`
* `GRAPHITE_REFRESH` - The fixed period (in ms) between checks. Default: 60000
* `GRAPHITE_USERNAME` - The HTTP Basic auth username for the Graphite server. Default: ``
* `GRAPHITE_PASSWORD` - The HTTP Basic auth password for the Graphite server. Default: ``
* `GRAPHITE_KEYSTORE` - The HTTP KeyStore path for the https Graphite server. Default: ``
* `GRAPHITE_KEYSTORE_PASSWORD` - The HTTP KeyStore password for the HTTPS Graphite server. Default: ``
* `GRAPHITE_TRUSTSTORE` - The HTTP TrustStore path for the https Graphite server. Default: ``
* `MONGO_URL` - The Mongo [connection string](http://docs.mongodb.org/manual/reference/connection-string/). Default: `mongodb://localhost:27017/seyren`
* `SEYREN_URL` - The location of your Seyren instance. Default: `http://localhost:8080/seyren`

#### [Real-time metrics](https://github.com/scobal/seyren/pull/142)
* `GRAPHITE_CARBON_PICKLE_ENABLE` - Enable a TCP server to listen Carbon relay [pickle protocol](http://graphite.readthedocs.org/en/latest/feeding-carbon.html). Default: `false`
* `GRAPHITE_CARBON_PICKLE_PORT` - The TCP server port. Default: `2004`

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

#### [IRCcat](https://github.com/RJ/irccat)
* `IRCCAT_HOST` - The hostname of the server where IRCcat is running. Default: `localhost`
* `IRCCAT_PORT` - The port on which IRCcat is running. Default: `12345`

#### Pushover
* `PUSHOVER_APP_API_TOKEN` - Your pushover App API Token

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

