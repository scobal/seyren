<img src="http://i.imgur.com/Ae5gQJZ.png" height="60" width="250" />

Seyren ([/ˈsaɪ.rʌn/](http://en.wikipedia.org/wiki/Wikipedia:IPA_for_English#Key)) is an alerting dashboard for Graphite.

[<img src="http://i.imgur.com/13nR3YA.png" height="504" width="700" />](http://i.imgur.com/ahu3aM6.png)

##Run

###Prerequisites

* Maven
* An instance of Graphite
* MongoDB ([Install instructions](http://docs.mongodb.org/manual/installation/#installation-guides Installing MongoDB))

###Stand alone

```
mvn clean package
export GRAPHITE_URL=http://graphite.foohost.com:80
java -jar seyren-web/target/seyren-web-*-war-exec.jar
open http://localhost:8080
```

If you want to change the port seyren runs on, you can use the jetty parameter:

```
java -jar seyren-web/target/dependency/jetty-runner.jar --port 9999 --path /seyren seyren-web/target/*.war
```

An example shell script of to run seyren is included as `run_seyren.sh`

###Environment variables

#### Base
* `GRAPHITE_URL` - The location of your Graphite server. Default: `http://localhost:80`
* `GRAPHITE_REFRESH` - The fixed period (in ms) between checks. Default: `60000`
* `GRAPHITE_USERNAME` - The HTTP Basic auth username for the Graphite server. Default: ``
* `GRAPHITE_PASSWORD` - The HTTP Basic auth password for the Graphite server. Default: ``
* `GRAPHITE_KEYSTORE` - The HTTP KeyStore path for the https Graphite server. Default: ``
* `GRAPHITE_KEYSTORE_PASSWORD` - The HTTP KeyStore password for the HTTPS Graphite server. Default: ``
* `GRAPHITE_TRUSTSTORE` - The HTTP TrustStore path for the https Graphite server. Default: ``
* `GRAPHITE_CONNECTION_REQUEST_TIMEOUT` - The number of millisconds to wait to obtain a connection from the pool. Default: `0` (infinite)
* `GRAPHITE_CONNECT_TIMEOUT` - The number of milliseconds to wait to establish a connection. Default: `0` (infinite)
* `GRAPHITE_SOCKET_TIMEOUT` - The number of milliseconds to wait for request data. Default: `0` (infinite)
* `MONGO_URL` - The Mongo [connection string](http://docs.mongodb.org/manual/reference/connection-string/). Default: `mongodb://localhost:27017/seyren`
* `SEYREN_URL` - The location of your Seyren instance. Default: `http://localhost:8080/seyren`
* `SEYREN_LOG_PATH` - The path of seyren.log. Default: ``. If a value is set, it must end with a '/'.

#### [Email](http://en.wikipedia.org/wiki/Simple_Mail_Transfer_Protocol)
* `SMTP_HOST` - The smtp server to send email notifications from. Default: `localhost`
* `SMTP_PORT` - The smtp server port. Default: `25`
* `SMTP_FROM` - The from email address for sending out notifications. Default: `alert@seyren`
* `SMTP_USERNAME` - The smtp server username if authenticated SMTP is used. Default: ``
* `SMTP_PASSWORD` - The smtp server password if authenticated SMTP is used. Default: ``
* `SMTP_PROTOCOL` - The smtp server protocol if authenticated SMTP is used. Default: `smtp`

#### [Flowdock](https://www.flowdock.com)
* `FLOWDOCK_EXTERNAL_USERNAME` - The username that messages will be sent from to a flow. Default: `Seyren`
* `FLOWDOCK_TAGS` -  Special tags to add to all messages. Default: ``
* `FLOWDOCK_EMOJIS` - Mapping between state and emojis unicode. Default: ``

#### [HipChat](https://www.hipchat.com)
* `HIPCHAT_AUTHTOKEN` - The hipchat api auth token. Default: ``
* `HIPCHAT_USERNAME` - The username that messages will be sent from. Default: `Seyren Alert`

#### [Hubot](http://hubot.github.com)
* `HUBOT_URL` - The location where Hubot is running. Default ``

#### [IRCcat](https://github.com/RJ/irccat)
* `IRCCAT_HOST` - The hostname of the server where IRCcat is running. Default: `localhost`
* `IRCCAT_PORT` - The port on which IRCcat is running. Default: `12345`

#### [PagerDuty](http://www.pagerduty.com)
* `PAGERDUTY_DOMAIN` - The PagerDuty domain to be notified. Default: ``
* `PAGERDUTY_USERNAME` - The PagerDuty API username. Default: ``
* `PAGERDUTY_PASSWORD` - The PagerDuty API Password. Default: ``

#### [Pushover](https://pushover.net)
* `PUSHOVER_APP_API_TOKEN` - Your pushover App API Token

#### [Real-time metrics](https://github.com/scobal/seyren/pull/142)
* `GRAPHITE_CARBON_PICKLE_ENABLE` - Enable a TCP server to listen Carbon relay [pickle protocol](http://graphite.readthedocs.org/en/latest/feeding-carbon.html). Default: `false`
* `GRAPHITE_CARBON_PICKLE_PORT` - The TCP server port. Default: `2004`

#### [SNMP](http://en.wikipedia.org/wiki/Simple_Network_Management_Protocol)
* `SNMP_HOST` - The SNMP host. Default: `localhost`
* `SNMP_PORT` - The SNMP port. Default: `162`
* `SNMP_COMMUNITY` - The SNMP  community. Default: `public`
* `SNMP_OID` - The SNMP OID. Default: `1.3.6.1.4.1.32473.1`

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

