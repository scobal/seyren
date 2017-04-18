<img src="http://i.imgur.com/Ae5gQJZ.png" height="45" width="200" />

Seyren ([/ˈsaɪ.rʌn/](http://en.wikipedia.org/wiki/Wikipedia:IPA_for_English#Key)) is an alerting dashboard for Graphite. It supports the following notification channels:

* [Email](http://en.wikipedia.org/wiki/Simple_Mail_Transfer_Protocol),
[Flowdock](https://www.flowdock.com),
[HipChat](https://www.hipchat.com),
[HTTP](http://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol),
[Hubot](http://hubot.github.com),
[IRCcat](https://github.com/RJ/irccat),
[PagerDuty](http://www.pagerduty.com),
[Pushover](https://pushover.net),
[SLF4J](http://www.slf4j.org),
[Slack](https://www.slack.com),
[SNMP](http://en.wikipedia.org/wiki/Simple_Network_Management_Protocol),
[Twilio](https://www.twilio.com/)


#[<img src="http://i.imgur.com/13nR3YA.png" height="200" width="280" />](http://i.imgur.com/ahu3aM6.png)

[![Build Status](https://travis-ci.org/scobal/seyren.svg?branch=master)](https://travis-ci.org/scobal/seyren)
[![Gitter chat](https://badges.gitter.im/scobal/seyren.png)](https://gitter.im/scobal/seyren)

### Prerequisites

* An instance of Graphite
* MongoDB ([Install instructions](http://docs.mongodb.org/manual/installation/#installation-guides Installing MongoDB))

### Run

```
wget https://github.com/scobal/seyren/releases/download/1.5.0/seyren-1.5.0.jar
export GRAPHITE_URL=http://graphite.foohost.com:80
java -jar seyren-1.5.0.jar
open http://localhost:8080
```

To run seyren on another port:

```
export SEYREN_URL="http://localhost:8081/seyren"
java -jar seyren-1.5.0.jar -httpPort=8081
```

### Config

The following options can be supplied as system properties or environment variables.

##### Base
* `MONGO_URL` - The Mongo [connection string](http://docs.mongodb.org/manual/reference/connection-string/). Default: `mongodb://localhost:27017/seyren`
* `SEYREN_URL` - The location of your Seyren instance. Default: `http://localhost:8080/seyren`
* `SEYREN_LOG_PATH` - The path of seyren.log. Default: ``. If a value is set, it must end with a '/'.
* `SEYREN_LOG_FILE_LEVEL` - The level of messages logged to the `file` appender (must correspond to a [Logback](http://logback.qos.ch/) log level. So one of `trace`, `debug`, `info`, `warn` or `error`). Default: `info`.
* `SEYREN_THREADS` - The number of pooled check threads to start. Default: `8`
* `GRAPHS_ENABLE` - Show(true) or hide(false) graphs in check page. Default: `true`.

##### [Graphite](http://graphite.readthedocs.org/en/latest/)
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

##### [Email](http://en.wikipedia.org/wiki/Simple_Mail_Transfer_Protocol)
* `SMTP_HOST` - The smtp server to send email notifications from. Default: `localhost`
* `SMTP_PORT` - The smtp server port. Default: `25`
* `SMTP_FROM` - The from email address for sending out notifications. Default: `alert@seyren`
* `SMTP_USERNAME` - The smtp server username if authenticated SMTP is used. Default: ``
* `SMTP_PASSWORD` - The smtp server password if authenticated SMTP is used. Default: ``
* `SMTP_PROTOCOL` - The smtp server protocol if authenticated SMTP is used. Default: `smtp`

##### [HTTP]
* `HTTP_NOTIFICATION_URL` - The url for a resource that contains a POST HTTP Method. Default: ``

##### [BigPanda.io](https://bigpanda.io)

Use a [Big Panda App Key](https://a.bigpanda.io/#/app/integrations/alertsapi/instructions/alertsapi) as Target when adding a new subscription.

* `BIGPANDA_NOTIFICATION_URL` - The url for a resource that contains a POST HTTP Method. Default: ``
* `BIGPANDA_AUTH_BEARER` - The Authorization Bearer header that is assigned to each customer by BigPanda. See [Alerts API integration](https://a.bigpanda.io/#/app/integrations/alertsapi/instructions/alertsapi) Default: ``

##### [Flowdock](https://www.flowdock.com)

Use a [Flow API token](https://www.flowdock.com/account/tokens) as Target when adding a new subscription.

* `FLOWDOCK_EXTERNAL_USERNAME` - The username that messages will be sent from to a flow. Default: `Seyren`
* `FLOWDOCK_TAGS` -  Special tags to add to all messages. Default: ``
* `FLOWDOCK_EMOJIS` - Mapping between state and emojis unicode. Default: ``

##### [HipChat](https://www.hipchat.com)
* `HIPCHAT_AUTHTOKEN` - The hipchat api auth token. Default: ``
* `HIPCHAT_USERNAME` - The username that messages will be sent from. Default: `Seyren Alert`

##### [Hubot](http://hubot.github.com)
* `HUBOT_URL` - The location where Hubot is running. Default ``

##### [IRCcat](https://github.com/RJ/irccat)
* `IRCCAT_HOST` - The hostname of the server where IRCcat is running. Default: `localhost`
* `IRCCAT_PORT` - The port on which IRCcat is running. Default: `12345`

##### [PagerDuty](http://www.pagerduty.com)
* No system properties or environment variables here, but in "Subscriptions dialog box", set the target field with a "Service API Key".
To generate a "Service API Key", see [PagerDuty Support: Adding Services](https://support.pagerduty.com/hc/en-us/articles/202828730-Adding-Services), section "Use our API directly".

##### [Pushover](https://pushover.net)
* `PUSHOVER_APP_API_TOKEN` - Your pushover App API Token

##### [Real-time metrics](https://github.com/scobal/seyren/pull/142)
* `GRAPHITE_CARBON_PICKLE_ENABLE` - Enable a TCP server to listen Carbon relay [pickle protocol](http://graphite.readthedocs.org/en/latest/feeding-carbon.html). Default: `false`
* `GRAPHITE_CARBON_PICKLE_PORT` - The TCP server port. Default: `2004`

##### [Slack](https://www.slack.com)

The target for a Slack subscription will be the channel name (including the `#`, for example `#channel`). You can optionally suffix the channel name with `!` and that will cause the alerts to include a `@channel` mention (for example `#channel!`).

* `SLACK_TOKEN` - The Slack api auth token. Default: ``
* `SLACK_USERNAME` - The username that messages will be sent to slack. Default: `Seyren`
* `SLACK_ICON_URL` - The user icon URL. Default: ``
* `SLACK_EMOJIS` - Mapping between state and emojis unicode. Default: ``

##### [SNMP](http://en.wikipedia.org/wiki/Simple_Network_Management_Protocol)
* `SNMP_HOST` - The SNMP host. Default: `localhost`
* `SNMP_PORT` - The SNMP port. Default: `162`
* `SNMP_COMMUNITY` - The SNMP  community. Default: `public`
* `SNMP_OID` - The SNMP OID. Default: `1.3.6.1.4.1.32473.1`

##### [TEMPLATE](http://en.wikipedia.org/wiki/Apache_Velocity)
* `TEMPLATE_EMAIL_FILE_PATH` - The path to the velocity template used when emailing an alert. Seyren will first attempt to load from the class path, but will fall back to loading from the filesystem.  Default: `com/seyren/core/service/notification/email-template.vm"`
* `TEMPLATE_EMAIL_SUBJECT_FILE_PATH` - The path to the velocity template used for subject when emailing an alert. Seyren will first attempt to load from the class path, but will fall back to loading from the filesystem.  Default: `com/seyren/core/service/notification/email-subject-template.vm"`

##### [Twilio](https://www.twilio.com/)
* `TWILIO_ACCOUNT_SID` - The Twilio Account SID. Default ``
* `TWILIO_AUTH_TOKEN` - The Twilio Auth Token. Default ``
* `TWILIO_PHONE_NUMBER` - The Twilio phone number to use to send SMS. Default ``
* `TWILIO_URL` - The Twilio API URL. Mostly useful for testing. Default `https://api.twilio.com/2010-04-01/Accounts`

##### [Victorops](https://www.victorops.com/)
* `VICTOROPS_REST_ENDPOINT` - The REST Endpoint given by Victorops. See [REST Integration]( http://victorops.force.com/knowledgebase/articles/Integration/Alert-Ingestion-API-Documentation/) Default ``

##### [Proxy](http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/impl/client/HttpClientBuilder.html#useSystemProperties())
The Proxy settings are changed by using JAVA_OPTS. It important that you exclude your graphite server or you will proxy these requests also.
* `JAVA_OPTS Example` JAVA_OPTS="-server -Dhttps.proxyHost=`PROXYSERVER` -Dhttps.proxyPort=`PROXYPORT` -Dhttp.proxyHost=`PROXYSERVER` -Dhttp.proxyPort=`PROXYPORT` -Dhttp.nonProxyHosts=`GRAPHITESERVER|*YOURDOMAIN.COM|INTERNALHIPCHAT`"

## Seyren API

See [Seyren API](API.md)

## Chef

You can use [Seyren Cookbook](https://github.com/obazoud/chef-seyren) to deploy Seyren with [Chef](http://www.getchef.com/).

## Docker
You can use the [Docker Seyren Image](https://registry.hub.docker.com/u/usman/docker-seyren/) to deploy a seyren instance in a [docker](https://docker.com) container.

## Docker compose
You can use `docker-compose` to create containers and hack seyren.
First, you need to install [Docker and Docker Compose](https://docs.docker.com/compose/#installation-and-set-up).
Then, you just need to start Docker Compose:
```
docker-compose up
```
and open [http://localhost:8080/seyren](http://localhost:8080/seyren).
Graphite is available on [http://localhost:8081/](http://localhost:8081/) with Basic Auth guest/guest.

## Init script

[Seyren-Init](https://github.com/wingZero21/Seyren-Init) is an init.d script which allows you to start the service.

## Development

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

To package up a new jar file without running the tests:

```
mvn package -DskipTests
# Set environment variables as needed.
java -jar seyren-web/target/seyren-web-*-war-exec.jar
```

## Related projects

* [Cerebro](https://github.com/voyages-sncf-technologies/cerebro) - Open alerting platform over Graphite (timeseries) and Seyren (scheduling)
